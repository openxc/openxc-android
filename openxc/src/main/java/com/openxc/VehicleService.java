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

import com.openxc.remote.RawEventMeasurement;
import com.openxc.remote.RawMeasurement;
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
    private Lock mRemoteBoundLock;
    private Condition mRemoteBoundCondition;

    private IBinder mBinder = new VehicleServiceBinder();
    private RemoteVehicleServiceInterface mRemoteService;
    private Multimap<Class<? extends VehicleMeasurement>,
            VehicleMeasurement.Listener> mListeners;
    private BiMap<String, Class<? extends VehicleMeasurement>>
            mMeasurementIdToClass;
    private BiMap<Class<? extends VehicleMeasurement>, String>
            mMeasurementClassToId;

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
                    RawEventMeasurement value) {
                Log.d(TAG, "Received " + measurementId + ": " +
                        value + " from remote service");

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

    public class VehicleServiceBinder extends Binder {
        public VehicleService getService() {
            return VehicleService.this;
        }
    }

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

        mListeners = HashMultimap.create();
        mListeners = Multimaps.synchronizedMultimap(mListeners);
        mMeasurementIdToClass = HashBiMap.create();
        mMeasurementClassToId = HashBiMap.create();
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

    private VehicleMeasurement getMeasurementFromRaw(
            Class<? extends VehicleMeasurement> measurementType,
            RawEventMeasurement rawMeasurement)
            throws UnrecognizedMeasurementTypeException{
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
        return constructBlankMeasurement(measurementType);
    }

    public VehicleMeasurement get(
            Class<? extends VehicleMeasurement> measurementType)
            throws UnrecognizedMeasurementTypeException {

        cacheMeasurementId(measurementType);

        if(mRemoteService == null) {
            Log.w(TAG, "Not connected to the RemoteVehicleService -- " +
                    "returning an empty measurement");
            return constructBlankMeasurement(measurementType);
        }

        Log.d(TAG, "Looking up measurement for " + measurementType);
        try {
            RawEventMeasurement rawMeasurement = mRemoteService.get(
                    mMeasurementClassToId.get(measurementType));
            return getMeasurementFromRaw(measurementType, rawMeasurement);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to get value from remote vehicle service", e);
            return constructBlankMeasurement(measurementType);
        }
    }

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
        }
    }

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
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("bound", mIsBound)
            .add("numListeners", mListeners.size())
            .toString();
    }
}
