package com.openxc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multimap;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleMeasurement;

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

    public RemoteVehicleServiceListenerInterface mRemoteListener =
        new RemoteVehicleServiceListenerInterface.Stub() {
            public void receiveNumerical(String measurementType, double value) {
                Log.d(TAG, "Received numerical " + measurementType + ": " +
                        value + " from remote service");
                // TODO will we look up the class by ID or by class name?
                // will that be done by the remote service or by us?
                // at startup, the remote service could create an index of ID to
                // class name, that would make it easier here. let's assume
                // we're passed the type name
                // TODO notify listeners of this type
            }

            public void receiveState(String measurementType, String state) {
                Log.d(TAG, "Received state " + measurementType + ": " +
                        state + " from remote service");
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

    public VehicleMeasurement get(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException {
        Constructor<? extends VehicleMeasurement> constructor;

        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the RemoteVehicleService -- " +
                    "returning an empty measurement");
            return constructBlankMeasurement(measurementType);
        }

        Log.d(TAG, "Looking up measurement for " + measurementType);
        try {
            constructor = measurementType.getConstructor(Double.class);
            Log.d(TAG, measurementType +  " has a numerical constructor " +
                    "-- using that");
            RawNumericalMeasurement rawMeasurement =
                mRemoteService.getNumericalMeasurement(
                        measurementType.toString());
            // TODO there is a lot of duplicated code in here - be smarted about
            // the use of an interface so we can only do this once
            if(rawMeasurement.isValid()) {
                Log.d(TAG, rawMeasurement +
                        " is valid, constructing a measurement with it");
                return constructor.newInstance(rawMeasurement.getValue());
            } else {
                Log.d(TAG, rawMeasurement +
                        " isn't valid -- returning a blank measurement");
                return constructBlankMeasurement(measurementType);
            }
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
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
        }

        try {
            constructor = measurementType.getConstructor(String.class);
            Log.d(TAG,
                    "Requested measurement type has a state-based constructor");
            RawStateMeasurement rawMeasurement =
                mRemoteService.getStateMeasurement(
                        measurementType.toString());
            if(rawMeasurement.isValid()) {
                return constructor.newInstance(rawMeasurement.getValue());
            } else {
                return constructBlankMeasurement(measurementType);
            }
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
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
        }

        return constructBlankMeasurement(measurementType);
    }

    public void addListener(Class<? extends VehicleMeasurement> measurementType,
            VehicleMeasurement.Listener listener)
            throws RemoteVehicleServiceException {
        Log.i(TAG, "Adding listener " + listener + " to " + measurementType);
        mListeners.put(measurementType, listener);
        try {
            mRemoteService.addListener(measurementType.toString(),
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
            mRemoteService.removeListener(measurementType.toString(),
                    mRemoteListener);
        } catch(RemoteException e) {
            throw new RemoteVehicleServiceException(
                    "Unable to unregister listener from remote vehicle service",
                    e);
        }
    }
}
