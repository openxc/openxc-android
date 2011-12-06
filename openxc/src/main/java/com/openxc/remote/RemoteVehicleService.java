package com.openxc.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.URISyntaxException;

import java.util.Collections;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.remote.sources.AbstractVehicleDataSourceCallback;

import com.openxc.remote.sources.usb.UsbVehicleDataSource;
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
        UsbVehicleDataSource.class.getName();

    private Map<String, RawMeasurement> mMeasurements;
    private VehicleDataSourceInterface mDataSource;

    private Map<String, RemoteCallbackList<
        RemoteVehicleServiceListenerInterface>> mListeners;
    private BlockingQueue<String> mNotificationQueue;
    private NotificationThread mNotificationThread;


    VehicleDataSourceCallbackInterface mCallback =
        new AbstractVehicleDataSourceCallback () {
            private void queueNotification(String measurementId) {
                if(mListeners.containsKey(measurementId)) {
                    try  {
                        mNotificationQueue.put(measurementId);
                    } catch(InterruptedException e) {}
                }
            }

            public void receive(final String measurementId,
                    final Double value) {
                mMeasurements.put(measurementId,
                        new RawMeasurement(value));
                queueNotification(measurementId);
            }

            public void receive(final String measurementId,
                    final Double value, final Double event) {
                mMeasurements.put(measurementId,
                        new RawMeasurement(value, event));
                queueNotification(measurementId);
            }
        };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        mMeasurements = new HashMap<String, RawMeasurement>();
        mNotificationQueue = new LinkedBlockingQueue<String>();

        mListeners = Collections.synchronizedMap(
                new HashMap<String, RemoteCallbackList<
                RemoteVehicleServiceListenerInterface>>());

        mNotificationThread = new NotificationThread();
        mNotificationThread.start();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service being destroyed");
        if(mDataSource != null) {
            mDataSource.stop();
        }

        if(mNotificationThread != null) {
            mNotificationThread.done();
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
            public RawMeasurement get(String measurementId)
                    throws RemoteException {
                return getMeasurement(measurementId);
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

    private class NotificationThread extends Thread {
        private boolean mRunning = true;

        public void done() {
            mRunning = false;
        }

        public void run() {
            while(mRunning) {
                String measurementId = null;
                try {
                    measurementId = mNotificationQueue.take();
                } catch(InterruptedException e) {}

                RemoteCallbackList<RemoteVehicleServiceListenerInterface>
                    callbacks = mListeners.get(measurementId);
                RawMeasurement rawMeasurement =
                    getMeasurement(measurementId);

                int i = callbacks.beginBroadcast();
                while(i > 0) {
                    i--;
                    try {
                        callbacks.getBroadcastItem(i).receive(measurementId,
                                rawMeasurement);
                    } catch(RemoteException e) {
                        Log.w(TAG, "Couldn't notify application " +
                                "listener -- did it crash?", e);
                    }
                }
                callbacks.finishBroadcast();
            }
        }
    };

    private RawMeasurement getMeasurement(String measurementId) {
        RawMeasurement rawMeasurement = mMeasurements.get(measurementId);
        if(rawMeasurement == null) {
            rawMeasurement = new RawMeasurement();
        }
        return rawMeasurement;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("dataSource", mDataSource)
            .add("numListeners", mListeners.size())
            .add("numMeasurementTypes", mMeasurements.size())
            .add("callbackBacklog", mNotificationQueue.size())
            .toString();
    }
}
