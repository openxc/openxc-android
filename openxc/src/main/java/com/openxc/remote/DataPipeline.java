package com.openxc.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.URISyntaxException;
import java.net.URI;

import java.util.concurrent.CopyOnWriteArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Objects;

import com.openxc.remote.sinks.DataSinkException;
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
    private int mMessagesReceived = 0;
    private Map<String, RawMeasurement> mMeasurements;
    private CopyOnWriteArrayList<VehicleDataSink> mSinks;
    private CopyOnWriteArrayList<VehicleDataSource> mSources;

    public DataPipeline(Context context) {
        mContext = context;
        mMeasurements = new HashMap<String, RawMeasurement>();
        mSinks = new CopyOnWriteArrayList<VehicleDataSink>();
        mSources = new CopyOnWriteArrayList<VehicleDataSource>();
    }

    public void receive(String measurementId, Object value, Object event) {
        for(Iterator<VehicleDataSink> i = mSinks.iterator(); i.hasNext();) {
            (i.next()).receive(measurementId, value, event);
        }
        mMessagesReceived++;
    }

    public void removeSink(VehicleDataSink sink) {
        if(sink != null) {
            mSinks.remove(sink);
            sink.stop();
        }
    }

    public VehicleDataSink addSink(VehicleDataSink sink) {
        mSinks.add(sink);
        return sink;
    }

    public void removeSink(String sinkName) {
        for(Iterator<VehicleDataSink> i = mSinks.iterator(); i.hasNext();) {
            VehicleDataSink sink = i.next();
            if(sink.getClass().getName().equals(sinkName)) {
                mSinks.remove(sink);
            }
        }
    }

    public VehicleDataSink addSink(String sinkName) throws DataSinkException {
        return addSink(sinkName, null);
    }

    // TODO do we add duplicate types? yes for now
    public VehicleDataSink addSink(String sinkName, String resource)
            throws DataSinkException {
        Class<? extends VehicleDataSink> sinkType;
        try {
            sinkType = Class.forName(sinkName).asSubclass(
                    VehicleDataSink.class);
        } catch(ClassNotFoundException e) {
            Log.w(TAG, "Couldn't find data sink type " + sinkName, e);
            throw new DataSinkException();
        }

        Constructor<? extends VehicleDataSink> constructor;
        try {
            constructor = sinkType.getConstructor(Context.class, Map.class);
        } catch(NoSuchMethodException e) {
            Log.w(TAG, sinkType + " doesn't have a proper constructor");
            throw new DataSinkException();
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

        mSinks.add(sink);
        return sink;
    }

    public void addSource(String sourceName) {
        addSource(sourceName, null);
    }

    // TODO do we add duplicate sources of the same type? yes for now, this will
    // screw up some tests that rely on it stopping the previous source
    public void addSource(String sourceName, String resource) {
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

        mSources.add(source);
    }

    public void stop() {
        clearSources();
        clearSinks();
    }

    public void clearSources() {
        for(Iterator<VehicleDataSource> i = mSources.iterator(); i.hasNext();) {
            (i.next()).stop();
        }
    }

    public void clearSinks() {
        for(Iterator<VehicleDataSink> i = mSinks.iterator(); i.hasNext();) {
            (i.next()).stop();
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

    public int getMessageCount() {
        return mMessagesReceived;
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("sources", mSources)
            .add("sinks", mSinks)
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
