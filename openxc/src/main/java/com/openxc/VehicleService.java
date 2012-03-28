package com.openxc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.Set;

import com.google.common.base.Objects;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multimap;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleMeasurement;

import com.openxc.remote.NoValueException;
import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceException;
import com.openxc.remote.RemoteVehicleService;
import com.openxc.remote.RemoteVehicleServiceInterface;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import android.content.Context;
import android.app.Service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import android.util.Log;

/**
 * The VehicleService is an in-process Android service and the primary entry
 * point into the OpenXC library.
 *
 * An OpenXC application should bind to this service and request vehicle
 * measurements through it either synchronously or asynchronously. The service
 * will shut down when no more clients are bound to it.
 *
 * Synchronous measurements are obtained by passing the type of the desired
 * measurement to the get method.
 *
 * Asynchronous measurements are obtained by defining a
 * VehicleMeasurement.Listener object and passing it to the service via the
 * addListener method.
 *
 * The source of vehicle data can be optionally selected by adding extra
 * parameters to the bind Intent. The parameters are passed wholesale on to the
 * {@link com.openxc.remote.RemoteVehicleService} - see that class's
 * documentation for details on the parameters.
 */
public class VehicleService extends Service {
    public final static String VEHICLE_LOCATION_PROVIDER =
            RemoteVehicleService.VEHICLE_LOCATION_PROVIDER;
    private final static String TAG = "VehicleService";

    private boolean mIsBound;
    private Lock mRemoteBoundLock;
    private Condition mRemoteBoundCondition;
    private String mDataSource;
    private String mDataSourceResource;
    private RecordingEnabledPreferenceListener mRecordingPreferenceListener;

    private IBinder mBinder = new VehicleServiceBinder();
    private RemoteVehicleServiceInterface mRemoteService;
    private Multimap<Class<? extends VehicleMeasurement>,
            VehicleMeasurement.Listener> mListeners;
    private BiMap<String, Class<? extends VehicleMeasurement>>
            mMeasurementIdToClass;
    private BiMap<Class<? extends VehicleMeasurement>, String>
            mMeasurementClassToId;

    /**
     * Binder to connect IBinder in a ServiceConnection with the VehicleService.
     *
     * This class is used in the onServiceConnected method of a
     * ServiceConnection in a client of this service - the IBinder given to the
     * application can be cast to the VehicleServiceBinder to retrieve the
     * actual service instance. This is required to actaully call any of its
     * methods.
     */
    public class VehicleServiceBinder extends Binder {
        /*
         * Return this Binder's parent VehicleService instance.
         *
         * @return an instance of VehicleService.
         */
        public VehicleService getService() {
            return VehicleService.this;
        }
    }

    /**
     * Block until the VehicleService is alive and can return measurements.
     *
     * Most applications don't need this and don't wait this method, but it can
     * be useful for testing when you need to make sure you will get a
     * measurement back from the system.
     */
    public void waitUntilBound() {
        mRemoteBoundLock.lock();
        Log.i(TAG, "Waiting for the RemoteVehicleService to bind to " + this);
        while(!mIsBound) {
            try {
                mRemoteBoundCondition.await();
            } catch(InterruptedException e) {}
        }
        Log.i(TAG, mRemoteService + " is now bound");
        mRemoteBoundLock.unlock();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");

        mRemoteBoundLock = new ReentrantLock();
        mRemoteBoundCondition = mRemoteBoundLock.newCondition();

        watchPreferences();

        mListeners = HashMultimap.create();
        mListeners = Multimaps.synchronizedMultimap(mListeners);
        mMeasurementIdToClass = HashBiMap.create();
        mMeasurementClassToId = HashBiMap.create();
        bindRemote();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        unwatchPreferences();
        unbindRemote();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        bindRemote();
        return mBinder;
    }

    /**
     * Retrieve a VehicleMeasurement from the current data source.
     *
     * Regardless of if a measurement is available or not, return a
     * VehicleMeasurement instance of the specified type. The measurement can be
     * checked to see if it has a value.
     *
     * @param measurementType The class of the requested VehicleMeasurement
     *      (e.g. VehicleSpeed.class)
     * @return An instance of the requested VehicleMeasurement which may or may
     *      not have a value.
     * @throws UnrecognizedMeasurementTypeException if passed a measurementType
     *      that does not extend VehicleMeasurement
     * @throws NoValueException if no value has yet been received for this
     *      measurementType
     * @see VehicleMeasurement
     */
    public VehicleMeasurement get(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException, NoValueException {

        cacheMeasurementId(measurementType);

        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the RemoteVehicleService -- " +
                    "throwing a NoValueException");
            throw new NoValueException();
        }

        Log.d(TAG, "Looking up measurement for " + measurementType);
        try {
            RawMeasurement rawMeasurement = mRemoteService.get(
                    mMeasurementClassToId.get(measurementType));
            return getMeasurementFromRaw(measurementType, rawMeasurement);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
            throw new NoValueException();
        }
    }

    /**
     * Register to receive async updates for a specific VehicleMeasurement type.
     *
     * Use this method to register an object implementing the
     * VehicleMeasurement.Listener interface to receive real-time updates
     * whenever a new value is received for the specified measurementType.
     *
     * @param measurementType The class of the VehicleMeasurement
     *      (e.g. VehicleSpeed.class) the listener was listening for
     * @param listener An VehicleMeasurement.Listener instance that was
     *      previously registered with addListener
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     * @throws UnrecognizedMeasurementTypeException if passed a measurementType
     *      not extend VehicleMeasurement
     */
    public void addListener(
            Class<? extends VehicleMeasurement> measurementType,
            VehicleMeasurement.Listener listener)
            throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        Log.i(TAG, "Adding listener " + listener + " to " + measurementType);
        cacheMeasurementId(measurementType);
        mListeners.put(measurementType, listener);

        if(mRemoteService != null) {
            try {
                mRemoteService.addListener(
                        mMeasurementClassToId.get(measurementType),
                        mRemoteListener);
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException(
                        "Unable to register listener with remote vehicle " +
                        "service", e);
            }
        } else {
            Log.w(TAG, "Can't add listener -- " +
                    "not connected to remote service yet");
        }
    }

    /**
     * Unregister a previously reigstered VehicleMeasurement.Listener instance.
     *
     * When an application is no longer interested in received measurement
     * updates (e.g. when it's pausing or exiting) it should unregister all
     * previously registered listeners to save on CPU.
     *
     * @param measurementType The class of the requested VehicleMeasurement
     *      (e.g. VehicleSpeed.class)
     * @param listener An object implementing the VehicleMeasurement.Listener
     *      interface that should be called with any new measurements.
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      registered with the library internals - an exceptional situation
     *      that shouldn't occur.
     * @throws UnrecognizedMeasurementTypeException if passed a class that does
     *      not extend VehicleMeasurement
     */
    public void removeListener(Class<? extends VehicleMeasurement>
            measurementType, VehicleMeasurement.Listener listener)
            throws RemoteVehicleServiceException {
        Log.i(TAG, "Removing listener " + listener + " from " +
                measurementType);
        mListeners.remove(measurementType, listener);
        if(mRemoteService != null) {
            try {
                mRemoteService.removeListener(
                        mMeasurementClassToId.get(measurementType),
                        mRemoteListener);
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException(
                        "Unable to unregister listener from remote " +
                        "vehicle service", e);
            }
        } else {
            Log.w(TAG, "Can't remove listener -- " +
                    "not connected to remote vehicle service yet");

        }
    }

    /**
     * Set and initialize the data source for the vehicle service.
     *
     * For example, to use the trace data source to playback a trace file, call
     * the setDataSource method after binding with VehicleService:
     *
     *      service.setDataSource(
     *              TraceVehicleDataSource.class.getName(),
     *              "resource://" + R.raw.tracejson);
     *
     * If no data source is specified (i.e. this method is never called), the
     * {@link UsbVehicleDataSource} will be used by default with the a default
     * USB device ID.
     *
     * @param dataSource The name of a class implementing the
     *      VehicleDataSourceInterface.
     * @param resource An optional initializer for the data source - this will
     *      be passed to its constructor as a String. An example is a path to a
     *      file if the data source is a trace file playback.
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public void setDataSource(String dataSource, String resource)
            throws RemoteVehicleServiceException {
        mDataSource = dataSource;
        mDataSourceResource = resource;

        if(mRemoteService != null) {
            try {
                Log.i(TAG, "Setting data source to " + dataSource);
                mRemoteService.setDataSource(dataSource, resource);
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException("Unable to set data " +
                        "source of remote vehicle service", e);
            }
        } else {
            Log.w(TAG, "Can't set data source -- " +
                    "not connected to remote service yet, but will set when " +
                    "connected");
        }
    }

    /**
     * Enable or disable recording of a trace file.
     *
     * @param enabled true if recording should be enabled
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public void enableRecording(boolean enabled)
            throws RemoteVehicleServiceException {
        if(mRemoteService != null) {
            try {
                Log.i(TAG, "Setting recording to " + enabled);
                mRemoteService.enableRecording(enabled);
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException("Unable to set " +
                        "recording status of remote vehicle service", e);
            }
        } else {
            Log.w(TAG, "Can't set recording status -- " +
                    "not connected to remote service yet, but will set when " +
                    "connected");
        }
    }

    /**
     * Read the number of messages received by the vehicle service.
     *
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public int getMessageCount() throws RemoteVehicleServiceException {
        if(mRemoteService != null) {
            try {
                return mRemoteService.getMessageCount();
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException(
                        "Unable to retrieve message count", e);
            }
        } else {
            throw new RemoteVehicleServiceException(
                    "Unable to retrieve message count");
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("bound", mIsBound)
            .add("numListeners", mListeners.size())
            .toString();
    }

    private void setRecordingStatus() {
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        boolean recordingEnabled = preferences.getBoolean(
                getString(R.string.recording_checkbox_key), false);
        try {
            enableRecording(recordingEnabled);
        } catch(RemoteVehicleServiceException e) {
            Log.w(TAG, "Unable to set recording status after binding", e);
        }
    }

    private void unwatchPreferences() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(
                mRecordingPreferenceListener);
    }

    private void watchPreferences() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        mRecordingPreferenceListener =
                new RecordingEnabledPreferenceListener();
        preferences.registerOnSharedPreferenceChangeListener(
                mRecordingPreferenceListener);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to RemoteVehicleService");
            mRemoteService = RemoteVehicleServiceInterface.Stub.asInterface(
                    service);

            mRemoteBoundLock.lock();
            mIsBound = true;
            mRemoteBoundCondition.signal();
            mRemoteBoundLock.unlock();

            // in case we had listeners registered before the remote service was
            // connected, sync up here.
            Set<Class<? extends VehicleMeasurement>> listenerKeys =
                mListeners.keySet();
            for(Class<? extends VehicleMeasurement> key : listenerKeys) {
                try {
                    mRemoteService.addListener(
                            mMeasurementClassToId.get(key),
                            mRemoteListener);
                    Log.i(TAG, "Added listener " + key +
                            " to remote vehicle service after it started up");
                } catch(RemoteException e) {
                    Log.w(TAG, "Unable to register listener with remote " +
                            "vehicle service", e);
                }
            }

            // in case the data source was set before being bound, set it again
            if(mDataSource != null && mDataSourceResource != null) {
                try {
                    setDataSource(mDataSource, mDataSourceResource);
                } catch(RemoteVehicleServiceException e) {
                    Log.w(TAG, "Unable to set data source after binding", e);
                }
            }
            setRecordingStatus();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "RemoteVehicleService disconnected unexpectedly");
            mRemoteService = null;
            mIsBound = false;
        }
    };

    private void cacheMeasurementId(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException {
        String measurementId;
        try {
            measurementId = (String) measurementType.getField("ID").get(
                    measurementType);
            mMeasurementIdToClass.put(measurementId, measurementType);
        } catch(NoSuchFieldException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " doesn't have an ID field", e);
        } catch(IllegalAccessException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " has an inaccessible ID", e);
        }
        mMeasurementClassToId = mMeasurementIdToClass.inverse();
    }

    private RemoteVehicleServiceListenerInterface mRemoteListener =
        new RemoteVehicleServiceListenerInterface.Stub() {
            public void receive(String measurementId,
                    RawMeasurement value) {
                Class<? extends VehicleMeasurement> measurementClass =
                    mMeasurementIdToClass.get(measurementId);
                VehicleMeasurement measurement;
                try {
                    measurement = getMeasurementFromRaw(measurementClass,
                            value);
                } catch(UnrecognizedMeasurementTypeException e) {
                    Log.w(TAG, "Received notification for a malformed " +
                            "measurement type: " + measurementClass, e);
                    return;
                } catch(NoValueException e) {
                    Log.w(TAG, "Received notification for a blank " +
                            "measurement of type: " + measurementClass, e);
                    return;
                }
                // TODO we may want to dump these in a queue handled by another
                // thread or post runnables to the main handler, sort of like we
                // do in the RemoteVehicleService. If the listener's receive
                // blocks...actually it might be OK.
                //
                // we do this in RVS because the data source would block waiting
                // for the receive to return before handling another.
                //
                // in this case we're being called from the handler thread in
                // RVS...so yeah, we don't want to block.
                //
                // AppLink posts runnables, but that might create a ton of
                // objects and be a lot of overhead. the queue method might be
                // fine, and if we use that we should see if the queue+notifying
                // thread setup can be abstracted and shared by the two
                // services.
                notifyListeners(measurementClass, measurement);
            }
        };

    private void notifyListeners(
            Class<? extends VehicleMeasurement> measurementType,
            VehicleMeasurement measurement) {
        synchronized(mListeners) {
            for(VehicleMeasurement.Listener listener :
                    mListeners.get(measurementType)) {
                listener.receive(measurement);
            }
        }
    }

    private void bindRemote() {
        Log.i(TAG, "Binding to RemoteVehicleService");
        Intent intent = new Intent(
                RemoteVehicleServiceInterface.class.getName());
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindRemote() {
        if(mRemoteBoundLock != null) {
            mRemoteBoundLock.lock();
        }

        if(mIsBound) {
            Log.i(TAG, "Unbinding from RemoteVehicleService");
            unbindService(mConnection);
            mIsBound = false;
        }

        if(mRemoteBoundLock != null) {
            mRemoteBoundLock.unlock();
        }
    }

    private VehicleMeasurement getMeasurementFromRaw(
            Class<? extends VehicleMeasurement> measurementType,
            RawMeasurement rawMeasurement)
            throws UnrecognizedMeasurementTypeException, NoValueException {
        Constructor<? extends VehicleMeasurement> constructor;
        try {
            constructor = measurementType.getConstructor(Double.class,
                    Double.class);
        } catch(NoSuchMethodException e) {
            constructor = null;
        }

        if(constructor == null) {
            try {
                constructor = measurementType.getConstructor(Double.class);
            } catch(NoSuchMethodException e) {
                throw new UnrecognizedMeasurementTypeException(measurementType +
                        " doesn't have a numerical constructor", e);
            }
        }

        if(rawMeasurement.isValid()) {
            try {
                if(rawMeasurement.hasEvent()) {
                    return constructor.newInstance(rawMeasurement.getValue(),
                            rawMeasurement.getEvent());
                } else {
                    return constructor.newInstance(rawMeasurement.getValue());
                }
            } catch(InstantiationException e) {
                throw new UnrecognizedMeasurementTypeException(
                        measurementType + " is abstract", e);
            } catch(IllegalAccessException e) {
                throw new UnrecognizedMeasurementTypeException(
                        measurementType + " has a private constructor", e);
            } catch(InvocationTargetException e) {
                throw new UnrecognizedMeasurementTypeException(
                        measurementType + "'s constructor threw an exception",
                        e);
            }
        } else {
            Log.d(TAG, rawMeasurement +
                    " isn't valid -- returning a blank measurement");
        }
        throw new NoValueException();
    }

    private class RecordingEnabledPreferenceListener
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.recording_checkbox_key))) {
                setRecordingStatus();
            }
        }
    }
}
