package com.openxc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multimap;

import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;

import com.openxc.remote.RawNumericalMeasurement;
import com.openxc.remote.RawStateMeasurement;
import com.openxc.remote.RemoteVehicleServiceException;
import com.openxc.remote.RemoteVehicleServiceInterface;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import android.content.Context;
import android.app.Service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import android.util.Log;

public class VehicleService extends Service {
    private final static String TAG = "VehicleService";

    private boolean mIsBound;

    private IBinder mBinder = new VehicleServiceBinder();
    private RemoteVehicleServiceInterface mRemoteService;
    private Multimap<Class<? extends VehicleMeasurement>,
            VehicleMeasurement.Listener> mListeners;
    private static final BiMap<String, Class<? extends VehicleMeasurement>>
            MEASUREMENT_ID_TO_CLASS;
    private static final BiMap<Class<? extends VehicleMeasurement>, String>
            MEASUREMENT_CLASS_TO_ID;

    static {
        MEASUREMENT_ID_TO_CLASS = HashBiMap.create();
        MEASUREMENT_ID_TO_CLASS.put(VehicleSpeed.ID, VehicleSpeed.class);
        MEASUREMENT_ID_TO_CLASS.put(SteeringWheelAngle.ID,
                SteeringWheelAngle.class);

        MEASUREMENT_CLASS_TO_ID = MEASUREMENT_ID_TO_CLASS.inverse();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to RemoteVehicleService");
            mRemoteService = RemoteVehicleServiceInterface.Stub.asInterface(
                    service);
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "RemoteVehicleService disconnected unexpectedly");
            mRemoteService = null;
            mIsBound = false;
        }
    };

    private RemoteVehicleServiceListenerInterface mRemoteListener =
        new RemoteVehicleServiceListenerInterface.Stub() {
            public void receiveNumerical(String measurementId,
                    RawNumericalMeasurement value) {
                Log.d(TAG, "Received numerical " + measurementId + ": " +
                        value + " from remote service");

                Class<? extends VehicleMeasurement> measurementClass =
                    MEASUREMENT_ID_TO_CLASS.get(measurementId);
                VehicleMeasurement measurement;
                try {
                    measurement = getNumericalMeasurementFromRaw(
                            measurementClass, value);
                } catch(UnrecognizedMeasurementTypeException e) {
                    Log.w(TAG, "Received notification for a malformed " +
                            "measurement type: " + measurementClass, e);
                    return;
                }
                notifyListeners(measurementClass, measurement);
            }

            public void receiveState(String measurementType,
                    RawStateMeasurement state) {
                Log.d(TAG, "Received state " + measurementType + ": " +
                        state + " from remote service");
                // TODO
            }
        };

    private void notifyListeners(
            Class<? extends VehicleMeasurement> measurementType,
            VehicleMeasurement measurement) {
        // TODO probably want to do a coarse lock around this
        for(VehicleMeasurement.Listener listener :
                mListeners.get(measurementType)) {
            listener.receive(measurement);
        }
    }

    public class VehicleServiceBinder extends Binder {
        VehicleService getService() {
            return VehicleService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");

        mListeners = HashMultimap.create();
        mListeners = Multimaps.synchronizedMultimap(mListeners);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        unbindRemote();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        bindRemote(intent);
        return mBinder;
    }

    private void bindRemote(Intent triggeringIntent) {
        Log.i(TAG, "Binding to RemoteVehicleService");
        Intent intent = new Intent(
                RemoteVehicleServiceInterface.class.getName());
        intent.putExtras(triggeringIntent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindRemote() {
        if(mIsBound) {
            Log.i(TAG, "Unbinding from RemoteVehicleService");
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private VehicleMeasurement constructBlankMeasurement(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException {
        try {
            return measurementType.newInstance();
        } catch(InstantiationException e) {
            throw new UnrecognizedMeasurementTypeException(
                    "No default constructor on given measurement type", e);
        } catch(IllegalAccessException e) {
            throw new UnrecognizedMeasurementTypeException(
                    "Default constructor not public on measurement type", e);
        }
    }

    private VehicleMeasurement getNumericalMeasurementFromRaw(
            Class<? extends VehicleMeasurement> measurementType,
            RawNumericalMeasurement rawMeasurement)
            throws UnrecognizedMeasurementTypeException{
        Constructor<? extends VehicleMeasurement> constructor;
        try {
            constructor = measurementType.getConstructor(
                    Double.class);
            Log.d(TAG, measurementType +  " has a numerical constructor " +
                    "-- using that");
        } catch(NoSuchMethodException e) {
            throw new UnrecognizedMeasurementTypeException(measurementType +
                    " doesn't have a numerical constructor", e);
        }

        if(rawMeasurement.isValid()) {
            Log.d(TAG, rawMeasurement +
                    " is valid, constructing a measurement with it");
            try {
                return constructor.newInstance(rawMeasurement.getValue());
            } catch(InstantiationException e) {
                throw new UnrecognizedMeasurementTypeException(
                        measurementType + " is abstract", e);
            } catch(IllegalAccessException e) {
                throw new UnrecognizedMeasurementTypeException(
                        measurementType + " has a private constructor", e);
            } catch(InvocationTargetException e) {
                throw new UnrecognizedMeasurementTypeException(
                        measurementType + "'s constructor threw an exception", e);
            }
        } else {
            Log.d(TAG, rawMeasurement +
                    " isn't valid -- returning a blank measurement");
        }
        return constructBlankMeasurement(measurementType);
    }

    public VehicleMeasurement get(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException {

        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the RemoteVehicleService -- " +
                    "returning an empty measurement");
            return constructBlankMeasurement(measurementType);
        }

        Log.d(TAG, "Looking up measurement for " + measurementType);
        try {
            RawNumericalMeasurement rawMeasurement =
                mRemoteService.getNumericalMeasurement(
                        MEASUREMENT_CLASS_TO_ID.get(measurementType));
            return getNumericalMeasurementFromRaw(measurementType,
                    rawMeasurement);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
            return constructBlankMeasurement(measurementType);
        }
    }

    public void addListener(Class<? extends VehicleMeasurement> measurementType,
            VehicleMeasurement.Listener listener)
            throws RemoteVehicleServiceException {
        Log.i(TAG, "Adding listener " + listener + " to " + measurementType);
        mListeners.put(measurementType, listener);
        try {
            mRemoteService.addListener(
                    MEASUREMENT_CLASS_TO_ID.get(measurementType),
                    mRemoteListener);
        } catch(RemoteException e) {
            throw new RemoteVehicleServiceException(
                    "Unable to register listener with remote vehicle service",
                    e);
        }
    }

    public void removeListener(Class<? extends VehicleMeasurement>
            measurementType, VehicleMeasurement.Listener listener)
            throws RemoteVehicleServiceException {
        Log.i(TAG, "Removing listener " + listener + " from " +
                measurementType);
        mListeners.remove(measurementType, listener);
        try {
            mRemoteService.removeListener(
                    MEASUREMENT_CLASS_TO_ID.get(measurementType),
                    mRemoteListener);
        } catch(RemoteException e) {
            throw new RemoteVehicleServiceException(
                    "Unable to unregister listener from remote vehicle service",
                    e);
        }
    }
}
