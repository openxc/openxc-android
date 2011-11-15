package com.openxc.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.URISyntaxException;

import java.util.Collections;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Map;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.remote.sources.ManualVehicleDataSource;
import com.openxc.remote.sources.VehicleDataSourceCallbackInterface;
import com.openxc.remote.sources.VehicleDataSourceInterface;

import android.app.Service;

import android.content.Context;
import android.content.Intent;

import java.net.URI;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import android.util.Log;

public class RemoteVehicleService extends Service {
    private final static String TAG = "RemoteVehicleService";
    public final static String DATA_SOURCE_NAME_EXTRA = "data_source";
    public final static String DATA_SOURCE_RESOURCE_EXTRA =
            "data_source_resource";
    private final static String DEFAULT_DATA_SOURCE =
        ManualVehicleDataSource.class.getName();

    private Map<String, Double> mNumericalMeasurements;
    private Map<String, String> mStateMeasurements;
    private VehicleDataSourceInterface mDataSource;

    private Map<String, RemoteCallbackList<
        RemoteVehicleServiceListenerInterface>> mListeners;
    private Lock mNotificationLock;

    VehicleDataSourceCallbackInterface mCallback =
        new VehicleDataSourceCallbackInterface() {
            public void receive(final String measurementId,
                    final double value) {
                mNumericalMeasurements.put(measurementId, value);
                new Thread() {
                    public void run() {
                        if(mListeners.containsKey(measurementId)) {
                            RemoteCallbackList<RemoteVehicleServiceListenerInterface>
                                callbacks = mListeners.get(measurementId);
                            mNotificationLock.lock();
                            int i = callbacks.beginBroadcast();
                            while(i > 0) {
                                i--;
                                try {
                                    callbacks.getBroadcastItem(i).receiveNumerical(
                                            measurementId,
                                            new RawNumericalMeasurement(value));
                                } catch(RemoteException e) {
                                    Log.w(TAG, "Couldn't notify application " +
                                            "listener -- did it crash?", e);
                                }
                            }
                        }
                    }}.start();
            }

            // TODO this is a bit of duplicated code from above, just changing
            // the numerical vs. state - function points would be really nice
            // here, but there is undoubtedly another way
            // TODO this is also now out of sync with the above method. eek!
            public void receive(String measurementId, String value) {
                mStateMeasurements.put(measurementId, value);
                if(mListeners.containsKey(measurementId)) {
                    RemoteCallbackList<RemoteVehicleServiceListenerInterface>
                        callbacks = mListeners.get(measurementId);
                    int i = callbacks.beginBroadcast();
                    while(i > 0) {
                        i--;
                        try {
                            callbacks.getBroadcastItem(i).receiveState(
                                    measurementId,
                                    new RawStateMeasurement(value));
                        } catch(RemoteException e) {
                            Log.w(TAG, "Couldn't notify application " +
                                    "listener -- did it crash?", e);
                        }
                    }
                }
            }
        };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        mNumericalMeasurements = new HashMap<String, Double>();
        mStateMeasurements = new HashMap<String, String>();
        mNotificationLock = new ReentrantLock();

        mListeners = Collections.synchronizedMap(
                new HashMap<String, RemoteCallbackList<
                RemoteVehicleServiceListenerInterface>>());
    }

    @Override
    public void onDestroy() {
        if(mDataSource != null) {
            mDataSource.stop();
        }
        // TODO loop over and kill all callbacks in remote callback list
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        Bundle extras = intent.getExtras();
        String dataSource = DEFAULT_DATA_SOURCE;
        String resource = null;
        if(extras != null) {
            dataSource = intent.getExtras().getString(
                    DATA_SOURCE_NAME_EXTRA);
            resource = intent.getExtras().getString(
                    DATA_SOURCE_RESOURCE_EXTRA);
        }
        initializeDataSource(dataSource, resource);
        return mBinder;
    }

    private VehicleDataSourceInterface initializeDataSource(
            String dataSourceName, String resource) {
        Class<? extends VehicleDataSourceInterface> dataSourceType;
        try {
            dataSourceType = Class.forName(dataSourceName).asSubclass(
                    VehicleDataSourceInterface.class);
        } catch(ClassNotFoundException e) {
            Log.w(TAG, "Couldn't find data source type " + dataSourceName, e);
            return null;
        }

        Constructor<? extends VehicleDataSourceInterface> constructor;
        try {
            constructor = dataSourceType.getConstructor(Context.class,
                    VehicleDataSourceCallbackInterface.class, URI.class);
        } catch(NoSuchMethodException e) {
            Log.w(TAG, dataSourceType + " doesn't have a proper constructor");
            return null;
        }

        URI resourceUri = null;
        if(resource != null) {
            try {
                resourceUri = new URI(resource);
            } catch(URISyntaxException e) {
                Log.w(TAG, "Unable to parse resource as URI " + resource);
            }
        }

        VehicleDataSourceInterface dataSource = null;
        try {
            dataSource = constructor.newInstance(this, mCallback, resourceUri);
        } catch(InstantiationException e) {
            Log.w(TAG, "Couldn't instantiate data source " + dataSourceType, e);
        } catch(IllegalAccessException e) {
            Log.w(TAG, "Default constructor is not accessible on " +
                    dataSourceType, e);
        } catch(InvocationTargetException e) {
            Log.w(TAG, dataSourceType + "'s constructor threw an exception",
                    e);
        }

        if(dataSource != null) {
            Log.i(TAG, "Initializing vehicle data source " + dataSource);
            new Thread(dataSource).start();
        }

        return dataSource;
    }

    private RemoteCallbackList<RemoteVehicleServiceListenerInterface>
            getOrCreateCallbackList(String measurementId) {
        RemoteCallbackList<RemoteVehicleServiceListenerInterface>
            callbackList = mListeners.get(measurementId);
        if(callbackList == null) {
            callbackList = new RemoteCallbackList<
                RemoteVehicleServiceListenerInterface>();
            mListeners.put(measurementId, callbackList);
        }
        return callbackList;
    }

    private final RemoteVehicleServiceInterface.Stub mBinder =
        new RemoteVehicleServiceInterface.Stub() {
            public RawNumericalMeasurement getNumericalMeasurement(
                    String measurementId) throws RemoteException {
                return new RawNumericalMeasurement(mNumericalMeasurements.get(
                        measurementId));
            }

            public RawStateMeasurement getStateMeasurement(
                    String measurementId) throws RemoteException {
                return new RawStateMeasurement(
                        mStateMeasurements.get(measurementId));
            }

            public void addListener(String measurementId,
                    RemoteVehicleServiceListenerInterface listener) {
                Log.i(TAG, "Adding listener " + listener + " to " +
                        measurementId);
                getOrCreateCallbackList(measurementId).register(listener);
            }

            public void removeListener(String measurementId,
                    RemoteVehicleServiceListenerInterface listener) {
                Log.i(TAG, "Removing listener " + listener + " from " +
                        measurementId);
                getOrCreateCallbackList(measurementId).unregister(listener);
            }
        };
}
