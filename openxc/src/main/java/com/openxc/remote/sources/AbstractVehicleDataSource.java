package com.openxc.remote.sources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;

import android.util.Log;

/**
 * The AbstractVehicleDataSource contains functions common to all vehicle data
 * sources.
 */
public abstract class AbstractVehicleDataSource
        implements VehicleDataSourceInterface {

    private static final String TAG = "AbstractVehicleDataSource";
    private static final String RECEIVE_METHOD_NAME = "receive";

    private VehicleDataSourceCallbackInterface mCallback;
    private Context mContext;

    public AbstractVehicleDataSource() { }

    /**
     * Construct a new instance with the given context and set the callback.
     *
     * @param context Current Android content (i.e. an Activity or Service)
     * @param callback An object implementing the
     *      VehicleDataSourceCallbackInterface that should receive data from this
     *      source.
     */
    public AbstractVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback) {
        mContext = context;
        setCallback(callback);
    }

    /**
     * Construct a new instance with no context and set the callback.
     *
     * @param callback An object implementing the
     *      VehicleDataSourceCallbackInterface that should receive data from this
     *      source.
     */
    public AbstractVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        this(null, callback);
    }

    public void setCallback(VehicleDataSourceCallbackInterface callback) {
        mCallback = callback;
    }

    protected void handleMessage(String name, Object value) {
        handleMessage(name, value, null);
    }

    protected void handleMessage(String name, Object value, Object event) {
        if(mCallback != null) {
            Method method;
            try {
                if(event != null) {
                    method = VehicleDataSourceCallbackInterface.class.getMethod(
                            RECEIVE_METHOD_NAME, String.class, Object.class,
                            Object.class);
                } else {
                    method = VehicleDataSourceCallbackInterface.class.getMethod(
                            RECEIVE_METHOD_NAME, String.class,
                            Object.class);
                }
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
                if(event != null) {
                    method.invoke(mCallback, name, value, event);
                } else {
                    method.invoke(mCallback, name, value);
                }
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

    protected VehicleDataSourceCallbackInterface getCallback() {
        return mCallback;
    }
}
