package com.openxc;

import java.util.concurrent.TimeUnit;
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

import com.google.common.base.MoreObjects;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.Command;
import com.openxc.messages.Command.CommandType;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.ExactKeyMatcher;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.MessageKey;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleService;
import com.openxc.remote.VehicleServiceException;
import com.openxc.remote.VehicleServiceInterface;
import com.openxc.remote.ViConnectionListener;
import com.openxc.sinks.MessageListenerSink;
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
 * measurement to the {@link #get(Class)} method. Asynchronous measurements are
 * obtained by defining a Measurement.Listener or VehicleMessage.Listener object
 * and passing it to the service via the addListener method.
 *
 * There are three major components in the VehicleManager:
 * {@link com.openxc.sources.VehicleDataSource},
 * {@link com.openxc.sinks.VehicleDataSink} and
 * {@link com.openxc.interfaces.VehicleInterface}.
 *
 * The instance of a {@link com.openxc.interfaces.VehicleInterface} is perhaps the
 * most important. This represents the actual physical connection to the
 * vehicle, and is bi-directional - it can both provide data to an
 * application and send data back to the vehicle. In most cases,
 * this should not be instantiated by applications; the
 * {@link #setVehicleInterface(Class, String)} method takes enough metadata for
 * the remote {@link com.openxc.remote.VehicleService} to instantiate the
 * interface in a remote process. That way a single USB or
 * Bluetooth connection can be shared among many applications.
 *
 * The VehicleManager also supports custom user-defined data sources, which can
 * be controlled with {@link #addSource(VehicleDataSource)} and
 * {@link #removeSource(VehicleDataSource)} methods. Even though data sources are
 * instantiated by the application, their data is still shared among all OpenXC
 * applications using the same remove process {@link
 * com.openxc.remote.VehicleService}
 *
 * In addition to applications registered to receive updates as a Measurement
 * or VehicleMessage listener, the VehicleManager supports custom data sinks (e.g.
 * {@link com.openxc.sinks.FileRecorderSink}) that be controlled with the
 * {@link #addSink(VehicleDataSink)} and {@link #removeSink(VehicleDataSink)}
 * methods.
 *
 * When a message is received from a
 * {@link com.openxc.sources.VehicleDataSource}, it is passed to every active
 * {@link com.openxc.sinks.VehicleDataSink}. There will always be at
 * least one sink that stores the latest messages and handles passing on data to
 * users of this service.
 */
public class VehicleManager extends Service implements DataPipeline.Operator {
    public final static String VEHICLE_LOCATION_PROVIDER =
            VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER;
    private final static String TAG = "VehicleManager";
    private Lock mRemoteBoundLock = new ReentrantLock();
    private Condition mRemoteBoundCondition = mRemoteBoundLock.newCondition();
    private IBinder mBinder = new VehicleBinder();

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
    private MessageListenerSink mNotifier = new MessageListenerSink();
    private UserSink mUserSink;

    /**
     * Binder to connect IBinder in a ServiceConnection with the VehicleManager.
     *
     * This class is used in the onServiceConnected method of a
     * ServiceConnection in a client of this service - the IBinder given to the
     * application can be cast to the VehicleBinder to retrieve the actual
     * service instance. This is required to actually call any of its methods.
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
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service unbinding in response to " + intent);
        return true;
    }

    /**
     * Block until the VehicleManager is alive and can return measurements.
     *
     * Blocks for at most 3 seconds.
     *
     * Most applications don't need this and don't wait this method, but it can
     * be useful for testing when you need to make sure you will get a
     * measurement back from the system.
     */
    public void waitUntilBound() throws VehicleServiceException {
        mRemoteBoundLock.lock();
        Log.i(TAG, "Waiting for the VehicleService to bind to " + this);
        while(mRemoteService == null) {
            try {
                if(!mRemoteBoundCondition.await(3, TimeUnit.SECONDS)) {
                    throw new VehicleServiceException(
                            "Not bound to remote service after 3 seconds");
                }
            } catch(InterruptedException e) {}
        }
        Log.i(TAG, mRemoteService + " is now bound");
        mRemoteBoundLock.unlock();
    }

    /**
     * Retrieve the most current value of a measurement.
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
        return BaseMeasurement.getMeasurementFromMessage(measurementType,
                get(BaseMeasurement.getKeyForMeasurement(measurementType)).asSimpleMessage());
    }

    /**
     * Retrieve the most current value of a keyed message.
     *
     * @param key The key of the requested Measurement
     *      (e.g. VehicleSpeed.class)
     * @return An instance of the requested Measurement which may or may
     *      not have a value.
     * @throws NoValueException if no value has yet been received for this
     *      measurementType
     * @see BaseMeasurement
     */
    public VehicleMessage get(MessageKey key) throws NoValueException {
        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the VehicleService -- " +
                    "throwing a NoValueException");
            throw new NoValueException();
        }

        try {
            VehicleMessage message = mRemoteService.get(key);
            if(message == null) {
                throw new NoValueException();
            }
            return message;
        } catch(RemoteException | ClassCastException e) {
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
            throw new NoValueException();
        }
    }

    /**
     * Send a message to the vehicle through the active
     * {@link com.openxc.interfaces.VehicleInterface} without waiting for
     * a response.
     *
     * @param message The desired message to send to the vehicle.
     * @return true if the message was sent successfully on an interface.
     */
    public boolean send(VehicleMessage message) {
        VehicleMessage wrappedMessage = message;
        if(message instanceof DiagnosticRequest) {
            // Wrap the request in a Command
            wrappedMessage = new Command(message.asDiagnosticRequest(),
                    DiagnosticRequest.ADD_ACTION_KEY);
        }

        boolean sent = false;
        // Don't want to keep this in the same list as local interfaces because
        // if that quits after the first interface reports success.
        if(mRemoteService != null) {
            try {
                sent = mRemoteService.send(wrappedMessage);
            } catch(RemoteException e) {
                Log.v(TAG, "Unable to propagate command to remote interface", e);
            }
        }

        if(sent) {
            // Add a timestamp of when the message was actually sent
            message.timestamp();
        }

        return sent;
    }

    /**
     * Convert a Measurement to a SimpleVehicleMessage and send it through the
     * active VehicleInterface.
     *
     * @param message The desired message to send to the vehicle.
     * @return true if the message was sent successfully on an interface.
     */
    public boolean send(Measurement message) {
        return send(message.toVehicleMessage());
    }

    /**
     * Send a message to the VehicleInterface and register the given listener to
     * receive the first response matching the message's key.
     *
     * This function is non-blocking.
     *
     * The listener is unregistered after the first response is received. If you
     * need to accept multiple responses for the same request, you must manually
     * register your own listener to control its lifecycle.
     *
     * @param message The desired message to send to the vehicle.
     * @param listener The message listener that should receive a callback when
     *      a response matching the outgoing message's key is received from the
     *      VI.
     */
    public void request(KeyedMessage message,
            VehicleMessage.Listener listener) {
        // Register the listener as non-persistent, so it is deleted after
        // receiving the first response
        mNotifier.register(ExactKeyMatcher.buildExactMatcher(message.getKey()),
                listener, false);
        send(message);
    }

    /**
     * Send a message to the VehicleInterface and wait up to 2 seconds to
     * receive a response.
     *
     * This is a blocking version of the other request(...) method.
     *
     * @return The response if one is received before the timeout, otherwise
     *      null.
     */
    public VehicleMessage request(KeyedMessage message) {
        BlockingMessageListener callback = new BlockingMessageListener();
        request(message, callback);
        return callback.waitForResponse();
    }

    /**
     * Register to receive asynchronous updates for a specific Measurement type.
     *
     * A Measurement is a specific, known VehicleMessage subtype.
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
     * @param listener An listener instance to receive the callback.
     */
    public void addListener(Class<? extends Measurement> measurementType,
            Measurement.Listener listener) {
        Log.i(TAG, "Adding listener " + listener + " for " + measurementType);
        mNotifier.register(measurementType, listener);
    }

    /**
     * Register to receive asynchronous updates for a specific VehicleMessage
     * type.
     *
     * Use this method to register an object implementing the
     * VehicleMessage.Listener interface to receive real-time updates
     * whenever a new value is received for the specified message type.
     *
     * Make sure you unregister your listeners with
     * VehicleManager.removeListener(...) when your activity or service closes
     * and no longer requires updates.
     *
     * @param messageType The class of the VehicleMessage
     *      (e.g. SimpleVehicleMessage.class) the listener is listening for
     * @param listener An listener instance to receive the callback.
     */
    public void addListener(Class<? extends VehicleMessage> messageType,
            VehicleMessage.Listener listener) {
        Log.i(TAG, "Adding listener " + listener + " for " + messageType);
        mNotifier.register(messageType, listener);
    }

    /**
     * Register to receive a callback when a message with same key as the given
     * KeyedMessage is received.
     *
     * @param keyedMessage A message with the key you want to receive updates
     *      for - the response to a command typically has the same key as the
     *      request, so you can use the outgoing message's KeyedMessage to
     *      register to receive a response.
     * @param listener An listener instance to receive the callback.
     */
    public void addListener(KeyedMessage keyedMessage,
            VehicleMessage.Listener listener) {
        addListener(keyedMessage.getKey(), listener);
    }

    /**
     * Register to receive a callback when a message with the given key is
     * received.
     *
     * @param key The key you want to receive updates.
     * @param listener An listener instance to receive the callback.
     */
    public void addListener(MessageKey key, VehicleMessage.Listener listener) {
        addListener(ExactKeyMatcher.buildExactMatcher(key), listener);
    }

    /**
     * Register to receive a callback when a message with key matching the given
     * KeyMatcher is received.
     *
     * This function can be used to set up a wildcard listener, or one that
     * receives a wider range of responses than just a 1 to 1 match of keys.
     *
     * @param matcher A KeyMatcher implement the desired filtering logic.
     * @param listener An listener instance to receive the callback.
     */
    public void addListener(KeyMatcher matcher, VehicleMessage.Listener listener) {
        Log.i(TAG, "Adding listener " + listener + " to " + matcher);
        mNotifier.register(matcher, listener);
    }

    /**
     * Unregister a previously registered Measurement.Listener instance.
     *
     * When an application is no longer interested in receiving measurement
     * updates (e.g. when it's pausing or exiting) it should unregister all
     * previously registered listeners to save on CPU.
     *
     * @param measurementType The class of the requested Measurement
     *      (e.g. VehicleSpeed.class)
     * @param listener The listener to remove.
     */
    public void removeListener(Class<? extends Measurement> measurementType,
            Measurement.Listener listener) {
        Log.i(TAG, "Removing listener " + listener + " for " + measurementType);
        mNotifier.unregister(measurementType, listener);
    }

    /**
     * Unregister a previously registered message type listener.
     *
     * @param messageType The class of the VehicleMessage this listener was
     *      registered to receive. A listener can be registered to receive
     *      multiple message types, which is why this must be specified when
     *      removing a listener.
     * @param listener The listener to remove.
     */
    public void removeListener(Class<? extends VehicleMessage> messageType,
            VehicleMessage.Listener listener) {
        mNotifier.unregister(messageType, listener);
    }

    /**
     * Unregister a previously registered keyed message listener.
     *
     * @param message The message with the key this listener was previously
     *      registered to receive.
     * @param listener The listener to remove.
     */
    public void removeListener(KeyedMessage message,
            VehicleMessage.Listener listener) {
        removeListener(message.getKey(), listener);
    }

    /**
     * Unregister a previously registered key matcher listener.
     *
     * @param matcher The KeyMatcher this listener was previously registered
     *      to receive matches from.
     * @param listener The listener to remove.
     */
    public void removeListener(KeyMatcher matcher,
            VehicleMessage.Listener listener) {
        mNotifier.unregister(matcher, listener);
    }

    /**
     * Unregister a previously registered key listener.
     *
     * @param key The key this listener was previously registered to
     *      receive updates on.
     * @param listener The listener to remove.
     */
    public void removeListener(MessageKey key, VehicleMessage.Listener listener) {
        removeListener(ExactKeyMatcher.buildExactMatcher(key), listener);
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
     * Send a command request to the vehicle that does not require any metadata.
     *
     * @param type The command request type to send to the VI.
     * @return The message returned by the VI in response to this command or
     *      null if none was received.
     */
    public String requestCommandMessage(CommandType type) {
        VehicleMessage message = request(new Command(type));
        String value = null;
        if(message != null) {
            // Because we use the same key and value for commands and command
            // responses, if for some reason a Command is echoed back to the
            // device instead of a CommandResponse, you could get a casting
            // exception when trying to cast this message here. If we got a
            // Command, just ignore it and assume no response - I wasn't able to
            // reproduce it but we did have a few Bugsnag reports about it.
            try {
                CommandResponse response = message.asCommandResponse();
                if(response.getStatus()) {
                    value = response.getMessage();
                }
            } catch(ClassCastException e) {
                Log.w(TAG, "Expected a command response but got " + message +
                        " -- ignoring, assuming no response");
            }
        }
        return value;
    }

    /**
     * Query for the unique device ID of the active VI.
     *
     * @return the device ID string or null if not known.
     */
    public String getVehicleInterfaceDeviceId() {
        return requestCommandMessage(CommandType.DEVICE_ID);
    }

    /**
     * Query for the firmware version of the active VI.
     *
     * @return the firmware version string or null if not known.
     */
    public String getVehicleInterfaceVersion() {
        return requestCommandMessage(CommandType.VERSION);
    }

    /**
     * Query for the platform of the active VI.
     *
     * @return the platform string or null if not known.
     */
    public String getVehicleInterfacePlatform() {
        return requestCommandMessage(CommandType.PLATFORM);
    }

    /**
     * Register a listener to receive a callback when the selected VI is
     * connected.
     *
     * @param listener The listener that should receive the callback.
     */
    public void addOnVehicleInterfaceConnectedListener(
            ViConnectionListener listener) throws VehicleServiceException {
        if(mRemoteService != null) {
            try {
                mRemoteService.addViConnectionListener(listener);
            } catch(RemoteException e) {
                throw new VehicleServiceException(
                        "Unable to add connection status listener", e);
            }
        }
    }

    /**
     * Change the active vehicle interface to a new type using its default
     * resource identifier.
     *
     * To disable all vehicle interfaces, pass null to this function.
     *
     * @param vehicleInterfaceType the VI type to activate or null to disable
     *      all VIs.
     */
    public void setVehicleInterface(
            Class<? extends VehicleInterface> vehicleInterfaceType)
            throws VehicleServiceException {
       setVehicleInterface(vehicleInterfaceType, null);
    }


    /**
     * Change the active vehicle interface to a new type using the given
     * resource.
     *
     * To disable all vehicle interfaces, pass null to this function.
     *
     * The only valid VehicleInterface types are those included with the library
     * - the vehicle service running in a remote process is the one to actually
     * instantiate the interfaces. Interfaces added with this method will be
     * available for all other OpenXC applications running in the system.
     *
     * @param vehicleInterfaceType A class implementing VehicleInterface that is
     *      included in the OpenXC library
     * @param resource A descriptor or a resource necessary to initialize the
     *      interface. See the specific implementation of {@link VehicleService}
     *      to find the required format of this parameter.
     */
    public void setVehicleInterface(
            Class<? extends VehicleInterface> vehicleInterfaceType,
            String resource) throws VehicleServiceException {
        Log.i(TAG, "Setting VI to: " + vehicleInterfaceType);

        String interfaceName = null;
        if(vehicleInterfaceType != null) {
            interfaceName = vehicleInterfaceType.getName();
        }

        if(mRemoteService != null) {
            try {
                mRemoteService.setVehicleInterface(interfaceName, resource);
            } catch(RemoteException e) {
                throw new VehicleServiceException(
                        "Unable to set vehicle interface", e);
            }
        } else {
            Log.w(TAG, "Can't set vehicle interface, not connected to the " +
                    "VehicleService");
        }
    }

    /**
     * Control whether the device's built-in GPS is used to provide location.
     *
     * @param enabled True if GPS should be read from the Android device and
     * injected as vehicle data whenever a vehicle interface is connected.
     */
    public void setNativeGpsStatus(boolean enabled) {
        Log.i(TAG, (enabled ? "Enabling" : "Disabling") + " native GPS");

        if(mRemoteService != null) {
            try {
                mRemoteService.setNativeGpsStatus(enabled);
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to change native GPS status", e);
            }
        } else {
            Log.w(TAG, "Not connected to the VehicleService");
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
     * Returns a descriptor of the active vehicle interface.
     *
     * @return A VehicleInterfaceDescriptor for the active VI or null if none is
     * enabled.
     */
    public VehicleInterfaceDescriptor getActiveVehicleInterface() {
        VehicleInterfaceDescriptor descriptor = null;
        if(mRemoteService != null) {
            try {
                descriptor = mRemoteService.getVehicleInterfaceDescriptor();
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to retrieve VI descriptor", e);
            }

        }
        return descriptor;
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
     * Return the connection status of the selected VI.
     *
     * @return true if the selected VI reports that it is connected to the
     * vehicle.
     */
    public boolean isViConnected() {
        if(mRemoteService != null) {
            try {
                return mUserOriginPipeline.isActive() || mRemoteService.isViConnected();
            } catch(RemoteException e) {
                Log.d(TAG, "Unable to send message to remote service", e);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("remoteService", mRemoteService)
            .toString();
    }

    @Override
    public void onPipelineActivated() {
        if(mRemoteService != null) {
            try {
                mRemoteService.userPipelineActivated();
            } catch(RemoteException e) {
                Log.d(TAG, "Unable to send message to remote service", e);
            }
        }
    }

    @Override
    public void onPipelineDeactivated() {
        if(mRemoteService != null) {
            try {
                mRemoteService.userPipelineDeactivated();
            } catch(RemoteException e) {
                Log.d(TAG, "Unable to send message to remote service", e);
            }
        }
    }

    private class BlockingMessageListener implements VehicleMessage.Listener {
        private final static int RESPONSE_TIMEOUT_S = 2;
        private Lock mLock = new ReentrantLock();
        private Condition mResponseReceived = mLock.newCondition();
        private VehicleMessage mResponse;

        public void receive(VehicleMessage message) {
            try {
                mLock.lock();
                mResponse = message;
                mResponseReceived.signal();
            } finally {
                mLock.unlock();
            }
        }

        public VehicleMessage waitForResponse() {
            try {
                mLock.lock();
                if(mResponse == null) {
                    mResponseReceived.await(RESPONSE_TIMEOUT_S, TimeUnit.SECONDS);
                }
            } catch(InterruptedException e) {
            } finally {
                mLock.unlock();
            }

            return mResponse;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleService");
            mRemoteService = VehicleServiceInterface.Stub.asInterface(service);

            mRemoteSource = new RemoteListenerSource(mRemoteService);
            mRemoteOriginPipeline.addSource(mRemoteSource);

            mUserSink = new UserSink(mRemoteService);
            mUserOriginPipeline.addSink(mUserSink);

            mRemoteBoundLock.lock();
            mRemoteBoundCondition.signalAll();
            mRemoteBoundLock.unlock();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mRemoteService = null;
            mRemoteOriginPipeline.removeSource(mRemoteSource);
            mUserOriginPipeline.removeSink(mUserSink);
            bindRemote();
        }
    };

    private void bindRemote() {
        Log.i(TAG, "Binding to VehicleService");
        Intent intent = new Intent(VehicleService.class.getName());
        intent.setComponent(new ComponentName("com.openxcplatform.enabler", "com.openxc.remote.VehicleService"));

        try {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        } catch(SecurityException e) {
            Log.e(TAG, "Unable to bind with remote service, it's not exported "
                    + "-- is the instrumentation tests package installed?", e);
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
