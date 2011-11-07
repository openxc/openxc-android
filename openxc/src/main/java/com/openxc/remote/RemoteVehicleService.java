package com.openxc.remote;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;

import android.content.Intent;

import android.os.IBinder;
import android.os.RemoteException;

import android.util.Log;

public class RemoteVehicleService extends Service {
    private final static String TAG = "RemoteVehicleService";

    private Map<String, Double> mNumericalMeasurements;
    private Map<String, String> mStateMeasurements;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        mNumericalMeasurements = new HashMap<String, Double>();
        mStateMeasurements = new HashMap<String, String>();
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
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
