package com.openxc.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.URISyntaxException;
import java.net.URI;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

import com.openxc.remote.sinks.DefaultDataSink;
import com.openxc.remote.sinks.VehicleDataSink;

import com.openxc.remote.sources.usb.UsbVehicleDataSource;

import com.openxc.remote.sources.VehicleDataSource;

import android.content.Context;

import android.util.Log;

/**
 * The DataPipeline ferries raw messages from VehicleDataSources to
 * VehicleDataSinks.
 */
public class DataPipeline {
    private final static String TAG = "DataPipeline";
    private final static String DEFAULT_DATA_SOURCE =
            UsbVehicleDataSource.class.getName();

    private Context mContext;
    private Map<String, RawMeasurement> mMeasurements;
    private VehicleDataSink mSink;
    private VehicleDataSource mSource;

    public DataPipeline(Context context) {
        mContext = context;
        mMeasurements = new HashMap<String, RawMeasurement>();
        mSink = new DefaultDataSink(getContext(), mMeasurements);
    }

    public void receive(String measurementId, Object value, Object event) {
        mSink.receive(measurementId, value, event);
    }

    public void setDefaultSource() {
        setSource(DEFAULT_DATA_SOURCE, null);
    }

    // TODO convert to addSource and support multiple
    public void setSource(String dataSourceName, String resource) {
        if(mSource != null) {
            mSource.stop();
            mSource = null;
        }

        Class<? extends VehicleDataSource> dataSourceType;
        try {
            dataSourceType = Class.forName(dataSourceName).asSubclass(
                    VehicleDataSource.class);
        } catch(ClassNotFoundException e) {
            Log.w(TAG, "Couldn't find data source type " + dataSourceName, e);
            return;
        }

        Constructor<? extends VehicleDataSource> constructor;
        try {
            constructor = dataSourceType.getConstructor(Context.class,
                    VehicleDataSink.class, URI.class);
        } catch(NoSuchMethodException e) {
            Log.w(TAG, dataSourceType + " doesn't have a proper constructor");
            return;
        }

        URI resourceUri = null;
        if(resource != null) {
            try {
                resourceUri = new URI(resource);
            } catch(URISyntaxException e) {
                Log.w(TAG, "Unable to parse resource as URI " + resource);
            }
        }

        VehicleDataSource dataSource = null;
        try {
            dataSource = constructor.newInstance(getContext(), this,
                    resourceUri);
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

        mSource = dataSource;
    }

    // TODO convert to addSink and support multiple
    public void setSink(VehicleDataSink sink) {
        mSink = sink;
    }

    public void stop() {
        if(mSource != null) {
            mSource.stop();
            mSource = null;
        }
    }

    public RawMeasurement get(String measurementId) {
        RawMeasurement rawMeasurement = mMeasurements.get(measurementId);
        if(rawMeasurement == null) {
            rawMeasurement = new RawMeasurement();
        }
        return rawMeasurement;
    }

    public boolean containsMeasurement(String measurementId) {
        return mMeasurements.containsKey(measurementId);
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("source", mSource)
            .add("numMeasurementTypes", mMeasurements.size())
            .toString();
    }
}
