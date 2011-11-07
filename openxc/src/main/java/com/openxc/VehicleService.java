package com.openxc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.openxc.measurements.VehicleMeasurement;

import com.openxc.remote.RemoteVehicleService;
import com.openxc.remote.RemoteVehicleServiceInterface;

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

    private IBinder mBinder = new VehicleServiceBinder();
    private RemoteVehicleServiceInterface mRemoteService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mRemoteService = RemoteVehicleServiceInterface.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            mRemoteService = null;
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
        Log.i(TAG, "Binding to RemoteVehicleService");
        bindService(new Intent(RemoteVehicleService.class.getName()),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        unbindService(mConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        return mBinder;
    }

    public VehicleMeasurement get(Class<VehicleMeasurement> measurementType)
            throws RemoteException {
        String measurementId;
        try {
            measurementId = (String)(
                    measurementType.getField("ID").get(measurementType));
        } catch(NoSuchFieldException e) {
            return null;
        } catch(IllegalAccessException e) {
            return null;
        }

        Constructor<VehicleMeasurement> constructor;
        try {
            constructor = measurementType.getConstructor(Double.class);
            return constructor.newInstance(
                    mRemoteService.getNumericalMeasurement(measurementId));
        } catch(NoSuchMethodException e) {
        } catch(InstantiationException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }

        try {
            constructor = measurementType.getConstructor(String.class);
            return constructor.newInstance(mRemoteService.getStateMeasurement(
                        measurementId));
        } catch(NoSuchMethodException e) {
        } catch(InstantiationException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        return null;

    }

    public void addListener(VehicleMeasurement.Listener listener) {
        Log.i(TAG, "Adding listener " + listener);
    }
}
