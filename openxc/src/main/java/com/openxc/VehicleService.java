package com.openxc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleMeasurement;

import com.openxc.remote.RemoteVehicleService;
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
    private Map<Class<? extends VehicleMeasurement>,
            VehicleMeasurement.Listener> mListeners;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to RemoteVehicleService");
            mRemoteService = RemoteVehicleServiceInterface.Stub.asInterface(
                    service);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "RemoteVehicleService disconnected unexpectedly");
            mRemoteService = null;
        }
    };

    // TODO this needs to be an injected dependency so we can override it when
    // testing and call these manually. make that change and instead of
    // having this be public and initializing it here, do it in the constructor
    public RemoteVehicleServiceListenerInterface mRemoteListener =
        new RemoteVehicleServiceListenerInterface.Stub() {
            public void receiveNumerical(String measurementType, double value) {
                // TODO will we look up the class by ID or by class name?
                // will that be done by the remote service or by us?
                // at startup, the remote service could create an index of ID to
                // class name, that would make it easier here. let's assume
                // we're passed the type name
                // TODO notify listeners of this type
            }

            public void receiveState(String measurementType, String state) {
                // TODO
            }
        };

    public class VehicleServiceBinder extends Binder {
        VehicleService getService() {
            return VehicleService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        bindRemote();

        mListeners = Collections.synchronizedMap(
                new HashMap<Class<? extends VehicleMeasurement>,
                VehicleMeasurement.Listener>());
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
        return mBinder;
    }

    private void bindRemote() {
        Log.i(TAG, "Binding to RemoteVehicleService");
        bindService(new Intent(RemoteVehicleService.class.getName()),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void unbindRemote() {
        if(mIsBound) {
            Log.i(TAG, "Unbinding from RemoteVehicleService");
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private String getMeasurementId(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException{
        try {
            String measurementId = (String)(
                    measurementType.getField("ID").get(measurementType));
            Log.d(TAG, measurementType + "'s ID is " + measurementId);
            return measurementId;
        } catch(NoSuchFieldException e) {
            throw new UnrecognizedMeasurementTypeException(
                    "No ID field on given measurement type", e);
        } catch(IllegalAccessException e) {
            throw new UnrecognizedMeasurementTypeException(
                    "ID field on given measurement type is not public", e);
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

    public VehicleMeasurement get(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException {
        String measurementId = getMeasurementId(measurementType);
        Constructor<? extends VehicleMeasurement> constructor = null;

        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the RemoteVehicleService -- " +
                    "returning an empty measurement");
            return constructBlankMeasurement(measurementType);
        }

        Log.d(TAG, "Looking up measurement for ID " + measurementId);
        try {
            constructor = measurementType.getConstructor(Double.class);
            Log.d(TAG, measurementType +  " has a numerical constructor " +
                    "-- using that");
            return constructor.newInstance(
                    mRemoteService.getNumericalMeasurement(measurementId));
        } catch(NoSuchMethodException e) {
            Log.d(TAG, measurementType +
                    " doesn't have a numerical constructor");
        } catch(InstantiationException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " is abstract", e);
        } catch(IllegalAccessException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " has a private constructor", e);
        } catch(InvocationTargetException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + "'s constructor threw an exception", e);
        } catch(RemoteException e) {
        }

        try {
            constructor = measurementType.getConstructor(String.class);
            Log.d(TAG,
                    "Requested measurement type has a state-based constructor");
            return constructor.newInstance(mRemoteService.getStateMeasurement(
                        measurementId));
        } catch(NoSuchMethodException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " must have a single argument " +
                    "constructor that accepts either a String or Double", e);
        } catch(InstantiationException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " is abstract", e);
        } catch(IllegalAccessException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " has a private constructor", e);
        } catch(InvocationTargetException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + "'s constructor threw an exception", e);
        } catch(RemoteException e) {
        }

        return constructBlankMeasurement(measurementType);
    }

    public void addListener(Class<? extends VehicleMeasurement> measurementType,
            VehicleMeasurement.Listener listener) {
        Log.i(TAG, "Adding listener " + listener + " to " + measurementType);
    }

    public void removeListener(Class<? extends VehicleMeasurement> measurementType,
            VehicleMeasurement.Listener listener) {
        Log.i(TAG, "Removing listener " + listener + " from " + measurementType);
    }
}
