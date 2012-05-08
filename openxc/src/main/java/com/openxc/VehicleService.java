package com.openxc;

import java.util.concurrent.CopyOnWriteArrayList;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.Set;

import com.google.common.base.Objects;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import com.openxc.measurements.MeasurementInterface;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.Measurement;

import com.openxc.remote.DataPipeline;
import com.openxc.remote.NoValueException;
import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceException;
import com.openxc.remote.RemoteVehicleServiceInterface;

import com.openxc.remote.sources.SourceCallback;
import com.openxc.remote.sources.VehicleDataSource;
import com.openxc.remote.sinks.MockedLocationSink;

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
 * Measurement.Listener object and passing it to the service via the
 * addListener method.
 */
public class VehicleService extends Service implements SourceCallback {
    public final static String VEHICLE_LOCATION_PROVIDER =
            MockedLocationSink.VEHICLE_LOCATION_PROVIDER;
    private final static String TAG = "VehicleService";

    private boolean mIsBound;
    private Lock mRemoteBoundLock;
    private Condition mRemoteBoundCondition;
    private PreferenceListener mPreferenceListener;
    private SharedPreferences mPreferences;

    private IBinder mBinder = new VehicleServiceBinder();
    private RemoteVehicleServiceInterface mRemoteService;
    private DataPipeline mPipeline;
    private RemoteListenerSource mRemoteSource;
    private ListenerSink mNotifier;
    private CopyOnWriteArrayList<VehicleDataSource> mSources;
    private BiMap<String, Class<? extends MeasurementInterface>>
            mMeasurementIdToClass;
    private BiMap<Class<? extends MeasurementInterface>, String>
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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = watchPreferences(mPreferences);

        mMeasurementIdToClass = HashBiMap.create();
        mMeasurementClassToId = HashBiMap.create();
        mPipeline = new DataPipeline();
        mNotifier = new ListenerSink(mMeasurementIdToClass);
        mPipeline.addSink(mNotifier);
        mSources = new CopyOnWriteArrayList<VehicleDataSource>();
        bindRemote();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        if(mPipeline != null) {
            mPipeline.stop();
        }
        unwatchPreferences(mPreferences, mPreferenceListener);
        unbindRemote();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        bindRemote();
        return mBinder;
    }

    /**
     * Retrieve a Measurement from the current data source.
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
     * @see Measurement
     */
    public MeasurementInterface get(
            Class<? extends MeasurementInterface> measurementType)
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
            return Measurement.getMeasurementFromRaw(measurementType,
                    rawMeasurement);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
            throw new NoValueException();
        }
    }

    /**
     * Register to receive async updates for a specific Measurement type.
     *
     * Use this method to register an object implementing the
     * Measurement.Listener interface to receive real-time updates
     * whenever a new value is received for the specified measurementType.
     *
     * @param measurementType The class of the Measurement
     *      (e.g. VehicleSpeed.class) the listener was listening for
     * @param listener An Measurement.Listener instance that was
     *      previously registered with addListener
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     * @throws UnrecognizedMeasurementTypeException if passed a measurementType
     *      not extend Measurement
     */
    public void addListener(
            Class<? extends MeasurementInterface> measurementType,
            MeasurementInterface.Listener listener)
            throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        Log.i(TAG, "Adding listener " + listener + " to " + measurementType);
        cacheMeasurementId(measurementType);
        mNotifier.register(measurementType, listener);

        if(mRemoteService != null) {
            try {
                mRemoteService.addListener(
                        mMeasurementClassToId.get(measurementType),
                        mRemoteSource.getListener());
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
     * Reset vehicle service to use only the default vehicle source.
     *
     * The default vehicle data source is USB. If a USB CAN translator is not
     * connected, there will be no more data.
     *
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public void initializeDefaultSources()
            throws RemoteVehicleServiceException {
        Log.i(TAG, "Resetting data sources");
        if(mRemoteService != null) {
            try {
                mRemoteService.initializeDefaultSources();
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException(
                        "Unable to reset data sources");
            }
        } else {
            Log.w(TAG, "Can't reset data sources -- " +
                    "not connected to remote service yet");
        }
    }

    public void clearSources() throws RemoteVehicleServiceException {
        Log.i(TAG, "Clearing all data sources");
        if(mRemoteService != null) {
            try {
                mRemoteService.clearSources();
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException(
                        "Unable to clear data sources");
            }
        } else {
            Log.w(TAG, "Can't clear all data sources -- " +
                    "not connected to remote service yet");
        }
        mSources.clear();
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
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      registered with the library internals - an exceptional situation
     *      that shouldn't occur.
     * @throws UnrecognizedMeasurementTypeException if passed a class that does
     *      not extend Measurement
     */
    public void removeListener(Class<? extends MeasurementInterface>
            measurementType, MeasurementInterface.Listener listener)
            throws RemoteVehicleServiceException {
        Log.i(TAG, "Removing listener " + listener + " from " +
                measurementType);
        mNotifier.unregister(measurementType, listener);
        if(mRemoteService != null) {
            try {
                mRemoteService.removeListener(
                        mMeasurementClassToId.get(measurementType),
                        mRemoteSource.getListener());
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
     * the addDataSource method after binding with VehicleService:
     *
     *      service.addDataSource(
     *              new TraceVehicleDataSource("/sdcard/openxc/trace.json"));
     *
     * If no data source is specified (i.e. this method is never called), the
     * {@link UsbVehicleDataSource} will be used by default with the a default
     * USB device ID.
     *
     * @param source an instance of a VehicleDataSource
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public void addDataSource(VehicleDataSource source)
            throws RemoteVehicleServiceException {
        Log.i(TAG, "Adding data source " + source);
        source.setCallback(this);
        mSources.add(source);
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
     * Enable or disable passing native host GPS through as vehicle
     * measurements.
     *
     * @param enabled true if native GPS should be passed through
     * @throws RemoteVehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional situation
     *      that shouldn't occur.
     */
    public void enableNativeGpsPassthrough(boolean enabled)
            throws RemoteVehicleServiceException {
        if(mRemoteService != null) {
            try {
                Log.i(TAG, "Setting native GPS to " + enabled);
                mRemoteService.enableNativeGpsPassthrough(enabled);
            } catch(RemoteException e) {
                throw new RemoteVehicleServiceException("Unable to set " +
                        "native GPS status of remote vehicle service", e);
            }
        } else {
            Log.w(TAG, "Can't set native GPS status -- " +
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
            .toString();
    }

    public void receive(String measurementId, Object value, Object event) {
        try {
            mRemoteService.receive(measurementId,
                    RawMeasurement.measurementFromObjects(value, event));
        } catch(RemoteException e) {
            Log.d(TAG, "Unable to send message to remote service", e);
        }
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

    private void setNativeGpsStatus() {
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        boolean nativeGpsEnabled = preferences.getBoolean(
                getString(R.string.native_gps_checkbox_key), false);
        try {
            enableNativeGpsPassthrough(nativeGpsEnabled);
        } catch(RemoteVehicleServiceException e) {
            Log.w(TAG, "Unable to set native GPS status after binding", e);
        }
    }

    private void unwatchPreferences(SharedPreferences preferences,
            PreferenceListener listener) {
        if(preferences != null && listener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    private PreferenceListener watchPreferences(SharedPreferences preferences) {
        if(preferences != null) {
            PreferenceListener listener = new PreferenceListener();
            preferences.registerOnSharedPreferenceChangeListener(
                    listener);
            return listener;
        }
        return null;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to RemoteVehicleService");
            mRemoteService = RemoteVehicleServiceInterface.Stub.asInterface(
                    service);

            mRemoteSource = new RemoteListenerSource();
            mPipeline.addSource(mRemoteSource);

            // in case we had listeners registered before the remote service was
            // connected, sync up here.
            Set<Class<? extends MeasurementInterface>> listenerKeys =
                mNotifier.getListeners().keySet();
            for(Class<? extends MeasurementInterface> key : listenerKeys) {
                try {
                    mRemoteService.addListener(
                            mMeasurementClassToId.get(key),
                            mRemoteSource.getListener());
                    Log.i(TAG, "Added listener " + key +
                            " to remote vehicle service after it started up");
                } catch(RemoteException e) {
                    Log.w(TAG, "Unable to register listener with remote " +
                            "vehicle service", e);
                }
            }

            setRecordingStatus();
            setNativeGpsStatus();

            mRemoteBoundLock.lock();
            mIsBound = true;
            mRemoteBoundCondition.signal();
            mRemoteBoundLock.unlock();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "RemoteVehicleService disconnected unexpectedly");
            mRemoteService = null;
            mIsBound = false;
            mPipeline.removeSource(mRemoteSource);
        }
    };

    private void cacheMeasurementId(
            Class<? extends MeasurementInterface> measurementType)
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

    private class PreferenceListener
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.recording_checkbox_key))) {
                setRecordingStatus();
            } else if(key.equals(getString(R.string.native_gps_checkbox_key))) {
                setNativeGpsStatus();
            }
        }
    }
}
