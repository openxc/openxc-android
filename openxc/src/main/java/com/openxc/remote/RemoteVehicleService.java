package com.openxc.remote;

import java.util.HashMap;
import java.util.Map;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.remote.sources.VehicleDataSourceInterface;
import com.openxc.remote.sources.VehicleDataSourceInterface;

import android.app.Service;

import android.content.Intent;

import android.os.IBinder;
import android.os.RemoteException;

import android.util.Log;

public class RemoteVehicleService extends Service {
    private final static String TAG = "RemoteVehicleService";
    public final static String DATA_SOURCE_NAME_EXTRA = "data_source";

    private Map<String, Double> mNumericalMeasurements;
    private Map<String, String> mStateMeasurements;
    private VehicleDataSourceInterface mDataSource;

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
        String dataSource = intent.getExtras().getString(
                DATA_SOURCE_NAME_EXTRA);
        initializeDataSource(dataSource);
        return mBinder;
    }

    private void initializeDataSource(String dataSourceName) {
        Class<? extends VehicleDataSourceInterface> dataSourceType;
        try {
            dataSourceType = Class.forName(dataSourceName).asSubclass(
                    VehicleDataSourceInterface.class);
        } catch(ClassNotFoundException e) {
            Log.w(TAG, "Couldn't find data source type " + dataSourceName, e);
            return;
        }

        try {
            mDataSource = dataSourceType.newInstance();
        } catch(InstantiationException e) {
            Log.w(TAG, "Couldn't instantiate data source " + dataSourceType, e);
        } catch(IllegalAccessException e) {
            Log.w(TAG, "Default constructo is not accessible on " +
                    dataSourceType, e);
        }
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

            public void addListener(
                    RemoteVehicleServiceListenerInterface listener) {
            }

            public void removeListener(
                    RemoteVehicleServiceListenerInterface listener) {
            }
        };
}
