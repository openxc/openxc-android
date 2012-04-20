package com.openxc.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.URISyntaxException;
import java.net.URI;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

import com.openxc.remote.sinks.VehicleDataSink;

import com.openxc.remote.sources.SourceCallback;

import com.openxc.remote.sources.VehicleDataSource;

import android.content.Context;

import android.util.Log;

/**
 * The DataPipeline ferries raw messages from VehicleDataSources to
 * VehicleDataSinks.
 */
public class DataPipeline implements SourceCallback {
    private final static String TAG = "DataPipeline";

    private Context mContext;
    private Map<String, RawMeasurement> mMeasurements;
    private VehicleDataSink mSink;
    private VehicleDataSource mSource;

    public DataPipeline(Context context) {
        mContext = context;
        mMeasurements = new HashMap<String, RawMeasurement>();
    }

    public void receive(String measurementId, Object value, Object event) {
        if(mSink != null) {
            mSink.receive(measurementId, value, event);
        }
    }

    public void setSink(String sinkName) {
        setSink(sinkName, null);
    }

    public void setSink(String sinkName, String resource) {
        if(mSink != null) {
            mSink.stop();
            mSink = null;
        }

        Class<? extends VehicleDataSink> sinkType;
        try {
            sinkType = Class.forName(sinkName).asSubclass(
                    VehicleDataSink.class);
        } catch(ClassNotFoundException e) {
            Log.w(TAG, "Couldn't find data sink type " + sinkName, e);
            return;
        }

        Constructor<? extends VehicleDataSink> constructor;
        try {
            constructor = sinkType.getConstructor(Context.class, Map.class);
        } catch(NoSuchMethodException e) {
            Log.w(TAG, sinkType + " doesn't have a proper constructor");
            return;
        }

        URI resourceUri = uriFromResourceString(resource);

        VehicleDataSink sink = null;
        try {
            sink = constructor.newInstance(getContext(), resourceUri);
        } catch(InstantiationException e) {
            Log.w(TAG, "Couldn't instantiate data sink " + sinkType, e);
        } catch(IllegalAccessException e) {
            Log.w(TAG, "Default constructor is not accessible on " +
                    sinkType, e);
        } catch(InvocationTargetException e) {
            Log.w(TAG, sinkType + "'s constructor threw an exception",
                    e);
        }

        if(sink != null) {
            Log.i(TAG, "Initializing vehicle data sink " + sink);
        }

        mSink = sink;
    }

    public void setSource(String sourceName) {
        setSource(sourceName, null);
    }

    // TODO convert to addSource and support multiple
    public void setSource(String sourceName, String resource) {
        if(mSource != null) {
            mSource.stop();
            mSource = null;
        }

        Class<? extends VehicleDataSource> sourceType;
        try {
            sourceType = Class.forName(sourceName).asSubclass(
                    VehicleDataSource.class);
        } catch(ClassNotFoundException e) {
            Log.w(TAG, "Couldn't find data source type " + sourceName, e);
            return;
        }

        Constructor<? extends VehicleDataSource> constructor;
        try {
            constructor = sourceType.getConstructor(Context.class,
                    VehicleDataSink.class, URI.class);
        } catch(NoSuchMethodException e) {
            Log.w(TAG, sourceType + " doesn't have a proper constructor");
            return;
        }

        URI resourceUri = uriFromResourceString(resource);

        VehicleDataSource source = null;
        try {
            source = constructor.newInstance(getContext(), this,
                    resourceUri);
        } catch(InstantiationException e) {
            Log.w(TAG, "Couldn't instantiate data source " + sourceType, e);
        } catch(IllegalAccessException e) {
            Log.w(TAG, "Default constructor is not accessible on " +
                    sourceType, e);
        } catch(InvocationTargetException e) {
            Log.w(TAG, sourceType + "'s constructor threw an exception",
                    e);
        }

        if(source != null) {
            Log.i(TAG, "Initializing vehicle data source " + source);
            new Thread(source).start();
        }

        mSource = source;
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

    private URI uriFromResourceString(String resource) {
        URI resourceUri = null;
        if(resource != null) {
            try {
                resourceUri = new URI(resource);
            } catch(URISyntaxException e) {
                Log.w(TAG, "Unable to parse resource as URI " + resource);
            }
        }
        return resourceUri;
    }
}
