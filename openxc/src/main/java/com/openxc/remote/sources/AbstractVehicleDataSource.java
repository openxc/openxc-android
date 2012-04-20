package com.openxc.remote.sources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;

import android.util.Log;

import com.openxc.remote.DataPipeline;

/**
 * The AbstractVehicleDataSource contains functions common to all vehicle data
 * sources.
 */
public abstract class AbstractVehicleDataSource implements VehicleDataSource {

    private static final String TAG = "AbstractVehicleDataSource";
    private static final String RECEIVE_METHOD_NAME = "receive";

    private DataPipeline mCallback;
    private Context mContext;

    public AbstractVehicleDataSource() { }

    /**
     * Construct a new instance with the given context and set the callback.
     *
     * @param context Current Android content (i.e. an Activity or Service)
     * @param callback An object implementing the
     *      DataPipeline interface that should receive data from this
     *      source.
     */
    public AbstractVehicleDataSource(Context context, DataPipeline callback) {
        mContext = context;
        setCallback(callback);
    }

    /**
     * Construct a new instance with no context and set the callback.
     *
     * @param callback An object implementing the
     *      DataPipeline that should receive data from this
     *      source.
     */
    public AbstractVehicleDataSource(
            DataPipeline callback) {
        this(null, callback);
    }

    public void setCallback(DataPipeline callback) {
        mCallback = callback;
    }

    protected void handleMessage(String name, Object value) {
        handleMessage(name, value, null);
    }

    protected void handleMessage(String name, Object value, Object event) {
        if(mCallback != null) {
            Method method;
            try {
                method = DataPipeline.class.getMethod(
                        RECEIVE_METHOD_NAME, String.class, Object.class,
                        Object.class);
            } catch(NoSuchMethodException e) {
                String logMessage = "Received data of an unsupported type " +
                    "from the data source: " + value + ", a " +
                    value.getClass();
                if(event != null) {
                    logMessage += " and event " + event + ", a " +
                        event.getClass();
                }
                Log.w(TAG, logMessage);
                return;
            }

            try {
                method.invoke(mCallback, name, value, event);
            } catch(IllegalAccessException e) {
                Log.w(TAG, "Data receiver method is private", e);
            } catch(InvocationTargetException e) {
                Log.w(TAG, "Unable to call data receive method", e);
            }
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected DataPipeline getCallback() {
        return mCallback;
    }
}
