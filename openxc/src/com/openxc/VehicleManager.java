package com.openxc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Objects;
import com.openxc.interfaces.InterfaceType;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.interfaces.VehicleInterfaceManagerUtils;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteServiceVehicleInterface;
import com.openxc.remote.VehicleService;
import com.openxc.remote.VehicleServiceException;
import com.openxc.remote.VehicleServiceInterface;
import com.openxc.sinks.MeasurementListenerSink;
import com.openxc.sinks.MockedLocationSink;
import com.openxc.sinks.UserSink;
import com.openxc.sinks.VehicleDataSink;
import com.openxc.sources.RemoteListenerSource;
import com.openxc.sources.VehicleDataSource;

/**
 * The VehicleManager is an in-process Android service and the primary entry
 * point into the OpenXC library.
 *
 * An OpenXC application should bind to this service and request vehicle
 * measurements through it either synchronously or asynchronously. The service
 * will shut down when no more clients are bound to it.
 *
 * Synchronous measurements are obtained by passing the type of the desired
 * measurement to the {@link #get(Class)} method.
 * Asynchronous measurements are obtained by defining a Measurement.Listener
 * object and passing it to the service via the addListener method.
 *
 *
 * There are three major components in the VehicleManager:
 * {@link com.openxc.sources.VehicleDataSource},
 * {@link com.openxc.sinks.VehicleDataSink} and
 * {@link com.openxc.interfaces.VehicleInterface}.
 *
 * The list of {@link com.openxc.interfaces.VehicleInterface} is perhaps the
 * most important. These instances represent actual physical connections to the
 * vehicle, and are bi-directional - they can both provide data to an
 * application and optionally send data back to the vehicle. In most cases,
 * these should not be instantiated by applications; the
 * {@link #addVehicleInterface(Class, String)} and
 * {@link #removeVehicleInterface(Class)} methods
 * take enough metadata from the remote {@link com.openxc.remote.VehicleService}
 * to instantiate the interface in a remove process. That way a single USB or
 * Bluetooth connection can be shared among many applications. If an application
 * really needs to use a custom VehicleInterface implementation and does not
 * mind if access to its write interface will be accessible only in the local
 * app process, the {@link #addLocalVehicleInterface(VehicleInterface)} and
 * {@link #removeLocalVehicleInterface(VehicleInterface)} can let you do that.
 *
 * The list of active data sources (e.g.
 * {@link com.openxc.sources.trace.TraceVehicleDataSource}) can be controlled
 * with the {@link #addSource(VehicleDataSource)} and
 * {@link #removeSource(VehicleDataSource)} methods. Each active
 * {@link com.openxc.interfaces.VehicleInterface} is also an active data source,
 * so there is no need to add them twice. Even though data sources are
 * instantiated by the application (and thus can be entirely customized), their
 * data is still shared among all OpenXC applications using the same remove
 * process {@link com.openxc.remote.VehicleService}
 *
 * The list of active data sinks (e.g.
 * {@link com.openxc.sinks.FileRecorderSink}) can be controlled with the
 * {@link #addSink(VehicleDataSink)} and {@link #removeSink(VehicleDataSink)}
 * methods.
 *
 * When a message is received from a
 * {@link com.openxc.sources.VehicleDataSource}, it is passed to all active
 * {@link com.openxc.sinks.VehicleDataSink}s. There will always be at
 * least one sink that stores the latest messages and handles passing on data to
 * users of this service.
 */
public class VehicleManager extends Service implements DataPipeline.Operator {
    public final static String VEHICLE_LOCATION_PROVIDER =
            MockedLocationSink.VEHICLE_LOCATION_PROVIDER;
    private final static String TAG = "VehicleManager";
    private Lock mRemoteBoundLock = new ReentrantLock();
    private Condition mRemoteBoundCondition = mRemoteBoundLock.newCondition();
    private IBinder mBinder = new VehicleBinder();
    private CopyOnWriteArrayList<VehicleInterface> mInterfaces =
            new CopyOnWriteArrayList<VehicleInterface>();

    // The mRemoteOriginPipeline in this class must only have 1 source - the
    // special RemoteListenerSource that receives measurements from the
    // VehicleService and propagates them to all of the user-registered sinks.
    private DataPipeline mRemoteOriginPipeline = new DataPipeline();

    // The mUserOriginPipeline, oppositely, must have only 1 sink - the special
    // UserSink that funnels data from user defined sources back to the
    // VehicleService. Any user-registered sources go in the mUserOriginPipeline
    // so they don't try to circumvent the VehicleService and send their values
    // directly to the in-process sinks (otherwise no other applications could
    // receive updates from that source).
    private DataPipeline mUserOriginPipeline = new DataPipeline(this);

    private boolean mIsBound;
    private VehicleServiceInterface mRemoteService;
    private RemoteListenerSource mRemoteSource;
    private VehicleInterface mRemoteController;
    private MeasurementListenerSink mNotifier = new MeasurementListenerSink();
    private UserSink mUserSink;

    /**
     * Binder to connect IBinder in a ServiceConnection with the VehicleManager.
     *
     * This class is used in the onServiceConnected method of a
     * ServiceConnection in a client of this service - the IBinder given to the
     * application can be cast to the VehicleBinder to retrieve the actual
     * service instance. This is required to actaully call any of its methods.
     */
    public class VehicleBinder extends Binder {
        /**
         * Return this Binder's parent VehicleManager instance.
         *
         * @return an instance of VehicleManager.
         */
        public VehicleManager getService() {
            return VehicleManager.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");

        mRemoteOriginPipeline.addSink(mNotifier);

        bindRemote();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        mRemoteOriginPipeline.stop();
        unbindRemote();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        bindRemote();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.i(TAG, "Service unbinding in response to " + intent);
        return true;
    }

    /**
     * Block until the VehicleManager is alive and can return measurements.
     *
     * Most applications don't need this and don't wait this method, but it can
     * be useful for testing when you need to make sure you will get a
     * measurement back from the system.
     */
    public void waitUntilBound() {
        mRemoteBoundLock.lock();
        Log.i(TAG, "Waiting for the VehicleService to bind to " + this);
        while(mRemoteService == null) {
            try {
                mRemoteBoundCondition.await();
            } catch(InterruptedException e) {}
        }
        Log.i(TAG, mRemoteService + " is now bound");
        mRemoteBoundLock.unlock();
    }

    /**
     * Retrieve the most current value of a measurement.
     *
     * Regardless of if a measurement is available or not, return a
     * Measurement instance of the specified type. The measurement can be
     * checked to see if it has a value.
     *
     * @param measurementType The class of the requested Measurement
     *      (e.g. VehicleSpeed.class)
     * @return An instance of the requested Measurement which may or may
     *      not have a value.
     * @throws UnrecognizedMeasurementTypeException if passed a measurementType
     *      that does not extend Measurement
     * @throws NoValueException if no value has yet been received for this
     *      measurementType
     * @see BaseMeasurement
     */
    public Measurement get(
            Class<? extends Measurement> measurementType)
            throws UnrecognizedMeasurementTypeException, NoValueException {

        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the VehicleService -- " +
                    "throwing a NoValueException");
            throw new NoValueException();
        }

        try {
            RawMeasurement rawMeasurement = mRemoteService.get(
                    BaseMeasurement.getIdForClass(measurementType));
            return BaseMeasurement.getMeasurementFromRaw(measurementType,
                    rawMeasurement);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
            throw new NoValueException();
        }
    }

    /**
     * Send a command to the vehicle through the first available active
     * {@link com.openxc.interfaces.VehicleInterface}.
     *
     * This will attempt to send the message over all of the registered vehicle
     * interfaces until one returns successfully. There is no guarantee about
     * the order that the interfaces are attempted.
     *
     * @param command The desired command to send to the vehicle.
     * @return true if the message was sent successfully
     */
    public boolean send(Measurement command) throws
                UnrecognizedMeasurementTypeException {
        return VehicleInterfaceManagerUtils.send(mInterfaces, command);
    }

    /**
     * Register to receive asynchronous updates for a specific Measurement type.
     *
     * Use this method to register an object implementing the
     * Measurement.Listener interface to receive real-time updates
     * whenever a new value is received for the specified measurementType.
     *
     * Make sure you unregister your listeners with
     * VehicleManager.removeListener(...) when your activity or service closes
     * and no longer requires updates.
     *
     * @param measurementType The class of the Measurement
     *      (e.g. VehicleSpeed.class) the listener was listening for
     * @param listener An Measurement.Listener instance that was
     *      previously registered with addListener
     * @throws VehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     * @throws UnrecognizedMeasurementTypeException if passed a measurementType
     *      not extend Measurement
     */
    public void addListener(Class<? extends Measurement> measurementType,
            Measurement.Listener listener) throws VehicleServiceException,
                UnrecognizedMeasurementTypeException {
        Log.i(TAG, "Adding listener " + listener + " to " + measurementType);
        mNotifier.register(measurementType, listener);
    }

    /**
     * Unregister a previously reigstered Measurement.Listener instance.
     *
     * When an application is no longer interested in received measurement
     * updates (e.g. when it's pausing or exiting) it should unregister all
     * previously registered listeners to save on CPU.
     *
     * @param measurementType The class of the requested Measurement
     *      (e.g. VehicleSpeed.class)
     * @param listener An object implementing the Measurement.Listener
     *      interface that should be called with any new measurements.
     * @throws VehicleServiceException if the listener is unable to be
     *      registered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public void removeListener(Class<? extends Measurement>
            measurementType, Measurement.Listener listener)
            throws VehicleServiceException {
        Log.i(TAG, "Removing listener " + listener + " from " +
                measurementType);
        mNotifier.unregister(measurementType, listener);
    }

    /**
     * Add a new data source to the vehicle service.
     *
     * For example, to use the trace data source to playback a trace file, call
     * the addSource method after binding with VehicleManager:
     *
     *      service.addSource(new TraceVehicleDataSource(
     *                  new URI("/sdcard/openxc/trace.json"))));
     *
     * @param source an instance of a VehicleDataSource
     */
    public void addSource(VehicleDataSource source) {
        Log.i(TAG, "Adding data source " + source);
        mUserOriginPipeline.addSource(source);
    }

    /**
     * Remove a previously registered source from the data pipeline.
     */
    public void removeSource(VehicleDataSource source) {
        if(source != null) {
            Log.i(TAG, "Removing data source " + source);
            mUserOriginPipeline.removeSource(source);
        }
    }

    /**
     * Add a new data sink to the vehicle service.
     *
     * A data sink added with this method will receive all new measurements as
     * they arrive from registered data sources.  For example, to use the trace
     * file recorder sink, call the addSink method after binding with
     * VehicleManager:
     *
     *      service.addSink(new FileRecorderSink(
     *              new AndroidFileOpener("openxc", this)));
     *
     * @param sink an instance of a VehicleDataSink
     */
    public void addSink(VehicleDataSink sink) {
        Log.i(TAG, "Adding data sink " + sink);
        mRemoteOriginPipeline.addSink(sink);
    }

    /**
     * Remove a previously registered sink from the data pipeline.
     */
    public void removeSink(VehicleDataSink sink) {
        if(sink != null) {
            mRemoteOriginPipeline.removeSink(sink);
            sink.stop();
        }
    }

    /**
     * Activate a vehicle interface for both receiving data and sending commands
     * to the vehicle.
     *
     * For example, to use a Bluetooth vehicle interface in addition to a
     * vehicle data source, call the addVehicleInterface method after binding
     * with VehicleManager:
     *
     *      service.addVehicleInterface(BluetoothVehicleInterface.class, "");
     *
     * The only valid VehicleInteface types are those included with the library
     * - the vehicle service running in a remote process is the one to actually
     * instantiate the interfaces. Interfaces added with this method will be
     * available for all other OpenXC applications running in the system. To use
     * custom implementations of {@link com.openxc.interfaces.VehicleInterface},
     * see {@link #addLocalVehicleInterface(VehicleInterface)} (but beware of
     * the caveats described with that method - interfaces added "locally" do
     * not support bidirectional communication for any other applications
     * besides the one that instantiated the interface).
     *
     * The {@link com.openxc.interfaces.usb.UsbVehicleInterface} is initialized
     * by default when the remote service starts, but it can be disabled with
     * {@link #removeVehicleInterface(Class)}.
     *
     * @param vehicleInterfaceType A class implementing VehicleInterface that is
     *      included in the OpenXC library
     * @param resource A descriptor or a resource necessary to initialize the
     *      interface. See the specific implementation of {@link VehicleService}
     *      to find the required format of this parameter.
     */
    public void addVehicleInterface(
            Class<? extends VehicleInterface> vehicleInterfaceType,
            String resource) {
        Log.i(TAG, "Adding interface: " + vehicleInterfaceType);

        if(mRemoteService != null) {
            try {
                mRemoteService.addVehicleInterface(
                        vehicleInterfaceType.getName(), resource);
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to add vehicle interface", e);
            }
        } else {
            Log.w(TAG, "Can't add vehicle interface, not connected to the " +
                    "VehicleService");
        }
    }

    /**
     * Disable a vehicle interface, stopping data flow in both directions.
     *
     * @param vehicleInterfaceType A class implementing VehicleInterface that is
     *      included in the OpenXC library, should have been previously added.
     */
    public void removeVehicleInterface(
            Class<? extends VehicleInterface> vehicleInterfaceType) {
        Log.i(TAG, "Removing interface: " + vehicleInterfaceType);

        if(mRemoteService != null) {
            try {
                mRemoteService.removeVehicleInterface(
                        vehicleInterfaceType.getName());
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to remove vehicle interface", e);
            }
        } else {
            Log.w(TAG, "Can't remove vehicle interface, not connected to the " +
                    "VehicleService");
        }
    }

    /**
     * Control whether polling is used to connect to a Bluetooth device or not.
     *
     * @param enabled True if polling should be used to connect to a Bluetooth
     * device.
     */
    public void setBluetoothPollingStatus(boolean enabled) {
        Log.i(TAG, (enabled ? "Enabling" : "Disabling") + " Bluetooth polling");

        if(mRemoteService != null) {
            try {
                mRemoteService.setBluetoothPollingStatus(enabled);
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to change Bluetooth polling status", e);
            }
        } else {
            Log.w(TAG, "Not connected to the VehicleService");
        }
    }

    /**
     * Return a list of all sources active in the system, suitable for
     * displaying in a status view.
     *
     * This method is soley for being able to peek into the system to see what's
     * active, which is why it returns strings intead of the actual source
     * objects. We don't want applications to be able to modify the sources
     * through this method.
     *
     * @return A list of the names and status of all sources.
     */
    public List<String> getSourceSummaries() {
        ArrayList<String> sources = new ArrayList<String>();
        for(VehicleDataSource source : mRemoteOriginPipeline.getSources()) {
            sources.add(source.toString());
        }

        for(VehicleDataSource source : mUserOriginPipeline.getSources()) {
            sources.add(source.toString());
        }

        if(mRemoteService != null) {
            try {
                sources.addAll(mRemoteService.getSourceSummaries());
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to retreive remote source summaries", e);
            }

        }
        return sources;
    }

    /**
     * Return a list of all sinks active in the system.
     *
     * The motivation for this method is the same as
     * {@link #getSinkSummaries()}.
     *
     * @return A list of the names and status of all sinks.
     */
    public List<String> getSinkSummaries() {
        ArrayList<String> sinks = new ArrayList<String>();
        for(VehicleDataSink sink : mRemoteOriginPipeline.getSinks()) {
            sinks.add(sink.toString());
        }

        if(mRemoteService != null) {
            try {
                sinks.addAll(mRemoteService.getSinkSummaries());
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to retreive remote sink summaries", e);
            }
        }
        return sinks;
    }

    /**
     * Returns a list of all active interface types
     *
     * @return A list of the InterfaceTypes actively connected.
     */
    public List<InterfaceType> getActiveSourceTypes(){
        ArrayList<InterfaceType> sources = new ArrayList<InterfaceType>();

        for(VehicleDataSource source : mUserOriginPipeline.getSources()) {
            if(source.isConnected()){
                sources.add(InterfaceType.interfaceTypeFromClass(source));
            }
        }

        for(VehicleDataSource source : mRemoteOriginPipeline.getSources()) {
            if(source.isConnected()){
                sources.add(InterfaceType.interfaceTypeFromClass(source));
            }
        }

        if(mRemoteService != null) {
            try {
                for(String sourceTypeString :
                        mRemoteService.getActiveSourceTypeStrings()) {
                    sources.add(InterfaceType.interfaceTypeFromString(
                                sourceTypeString));
                }
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to retreive remote source summaries", e);
            }

        }
        return sources;
    }

    /**
     * Read the number of messages received by the vehicle service.
     *
     * @throws VehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public int getMessageCount() throws VehicleServiceException {
        if(mRemoteService != null) {
            try {
                return mRemoteService.getMessageCount();
            } catch(RemoteException e) {
                throw new VehicleServiceException(
                        "Unable to retrieve message count", e);
            }
        } else {
            throw new VehicleServiceException(
                    "Unable to retrieve message count");
        }
    }

    /**
     * Add a new local vehicle interface to the service.
     *
     * This method will accept any implementation of {@link VehicleInterface},
     * so completely custom implementations are possible beyond the few built-in
     * to the OpenXC library.
     *
     * However, interfaces added with this method cannot
     * be used to send commands to the vehicle by any other active OpenXC
     * application besides the one that instantiates the
     * {@link VehicleInterface} object.
     *
     * That limitation does not apply to data received from the VehicleInterface
     * - it will be propagated to all other applications, although there is an
     * additional performance hit when compared to the built-in VehicleInterface
     * types because Android's AIDL barrier must be crossed twice instead of
     * once to communicate with the remote {@link VehicleService}.
     *
     * @param vehicleInterface an instance of a VehicleInteface
     */
    public void addLocalVehicleInterface(VehicleInterface vehicleInterface) {
        if(vehicleInterface != null) {
            Log.i(TAG, "Adding local vehicle interface " + vehicleInterface);
            mInterfaces.add(vehicleInterface);
            mRemoteOriginPipeline.addSource(vehicleInterface);
        }
    }

    /**
     * Remove a previously registered local vehicle interface from the
     * service.
     *
     * Data from the {@link VehicleInterface} will no longer be propagated to
     * applications, and it will no longer be available for sending commands to
     * the vehicle.
     */
    public void removeLocalVehicleInterface(VehicleInterface vehicleInterface) {
        if(vehicleInterface != null) {
            mInterfaces.remove(vehicleInterface);
            mRemoteOriginPipeline.removeSource(vehicleInterface);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("remoteService", mRemoteService)
            .toString();
    }

    public void onPipelineActivated() {
        if(mRemoteService != null) {
            try {
                mRemoteService.userPipelineActivated();
            } catch(RemoteException e) {
                Log.d(TAG, "Unable to send message to remote service", e);
            }
        }
    }

    public void onPipelineDeactivated() {
        if(mRemoteService != null) {
            try {
                mRemoteService.userPipelineDeactivated();
            } catch(RemoteException e) {
                Log.d(TAG, "Unable to send message to remote service", e);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleService");
            mRemoteService = VehicleServiceInterface.Stub.asInterface(service);
            mRemoteController = new RemoteServiceVehicleInterface(
                    mRemoteService);
            mInterfaces.add(mRemoteController);

            mRemoteSource = new RemoteListenerSource(mRemoteService);
            mRemoteOriginPipeline.addSource(mRemoteSource);

            mUserSink = new UserSink(mRemoteService);
            mUserOriginPipeline.addSink(mUserSink);

            mRemoteBoundLock.lock();
            mRemoteBoundCondition.signalAll();
            mRemoteBoundLock.unlock();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mInterfaces.remove(mRemoteController);
            mRemoteService = null;
            mRemoteOriginPipeline.removeSource(mRemoteSource);
            mUserOriginPipeline.removeSink(mUserSink);
            bindRemote();
        }
    };

    private void bindRemote() {
        Log.i(TAG, "Binding to VehicleService");
        // TODO Android warns us that this implicit intent is unsafe, but if we
        // try creating an explicit intent (with out context), the server is
        // never bound. What gives?
        Intent intent = new Intent(VehicleServiceInterface.class.getName());
        try {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        } catch(SecurityException e) {
            Log.e(TAG, "Unable to bind with remote service, it's not exported "
                    + "-- is the instrumentation tests package installed?", e);
            Toast.makeText(this, "Vehicle service is not exported and is " +
                    "inaccessible - are the instrumentation tests still " +
                    "installed?", Toast.LENGTH_LONG).show();
        }
    }

    private void unbindRemote() {
        if(mRemoteBoundLock != null) {
            mRemoteBoundLock.lock();
        }

        if(mIsBound) {
            Log.i(TAG, "Unbinding from VehicleService");
            unbindService(mConnection);
            mRemoteService = null;
            mIsBound = false;
        }

        if(mRemoteBoundLock != null) {
            mRemoteBoundLock.unlock();
        }
    }
}
