package com.openxc.remote;

import java.util.HashMap;
import java.util.Map;

import com.openxc.measurements.VehicleMeasurement;

import android.app.Service;

import android.content.Intent;

import android.os.IBinder;
import android.os.RemoteException;

public class RemoteVehicleService extends Service {

    private Map<String, Double> mNumericalMeasurements;
    private Map<String, String> mStateMeasurements;

    @Override
    public void onCreate() {
        super.onCreate();
        mNumericalMeasurements = new HashMap<String, Double>();
        mStateMeasurements = new HashMap<String, String>();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final RemoteVehicleServiceInterface.Stub mBinder =
        new RemoteVehicleServiceInterface.Stub() {
            public double getNumericalMeasurement(String measurementId)
                    throws RemoteException {
                return mNumericalMeasurements.get(measurementId).doubleValue();
            }

            public String getStateMeasurement(String measurementId) {
                return mStateMeasurements.get(measurementId);
            }
        };
}
