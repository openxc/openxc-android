package com.openxc;

import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleMeasurement;

import com.openxc.remote.RemoteVehicleService;
import com.openxc.remote.RemoteVehicleServiceInterface;

import com.openxc.units.Unit;

import android.content.Context;
import android.app.Service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Binder;
import android.os.IBinder;

public class VehicleService extends Service {
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
        bindService(new Intent(RemoteVehicleService.class.getName()),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Measurement<Unit> get(Class<VehicleMeasurement> measurementType) {
        return new Measurement<Unit>(mRemoteService.getNumericalMeasurement((String)(
                    measurementType.getField("ID").get(measurementType))));
    }

    public void addListener(VehicleMeasurement.Listener listener) {
    }
}
