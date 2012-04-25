package com.openxc.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.URISyntaxException;
import java.net.URI;

import java.util.ArrayList;

import java.util.concurrent.CopyOnWriteArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

import com.openxc.remote.sinks.DataSinkException;
import com.openxc.remote.sinks.VehicleDataSink;

import com.openxc.remote.sources.DataSourceException;
import com.openxc.remote.sources.SourceCallback;
import com.openxc.remote.sources.VehicleDataSource;

/**
 * The DataPipeline ferries raw messages from VehicleDataSources to
 * VehicleDataSinks.
 */
public class DataPipeline implements SourceCallback {
    private final static String TAG = "DataPipeline";

    private int mMessagesReceived = 0;
    // TODO move all of the measurement stuff to a sink
    // and have the RVS create and handle that sink itself
    private Map<String, RawMeasurement> mMeasurements;
    private CopyOnWriteArrayList<VehicleDataSink> mSinks;
    private CopyOnWriteArrayList<VehicleDataSource> mSources;

    public DataPipeline() {
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

    public void removeSink(String sinkName) {
        removeEndpoint(mSinks, sinkName);
    }

    public VehicleDataSink addSink(VehicleDataSink sink) {
        mSinks.add(sink);
        return sink;
    }


    public List<VehicleDataSink> getSinks() {
        return mSinks;
    }

    public List<VehicleDataSource> getSources() {
        return mSources;
    }

    public void addSource(VehicleDataSource source) {
        source.setCallback(this);
        mSources.add(source);
    }

    public void removeSource(VehicleDataSource source) {
        if(source != null) {
            mSources.remove(source);
            source.stop();
        }
    }

    public void removeSource(String sourceName) {
        removeEndpoint(mSources, sourceName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void removeEndpoint(CopyOnWriteArrayList endpoints,
            String endpointName) {
        for(Iterator<VehicleDataEndpoint> i = endpoints.iterator();
                i.hasNext();) {
            VehicleDataEndpoint endpoint = i.next();
            if(endpoint.getClass().getName().equals(endpointName)) {
                endpoint.stop();
                endpoints.remove(endpoint);
            }
        }
    };

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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("sources", mSources)
            .add("sinks", mSinks)
            .add("numMeasurementTypes", mMeasurements.size())
            .toString();
    }

}
