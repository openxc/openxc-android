package com.openxc;

import java.util.concurrent.CopyOnWriteArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Objects;

import com.openxc.remote.RawMeasurement;

import com.openxc.sinks.VehicleDataSink;

import com.openxc.sources.SourceCallback;
import com.openxc.sources.VehicleDataSource;

/**
 * A pipeline that ferries data from VehicleDataSources to VehicleDataSinks.
 *
 * A DataPipeline accepts two types of components - sources and sinks. The
 * sources (implementing {@link VehicleDataSource} call the
 * {@link #receive(String, Object, Object)} method on the this class when new
 * values arrive. The DataPipeline then passes this value on to all currently
 * registered data sinks.
 */
public class DataPipeline implements SourceCallback {
    private int mMessagesReceived = 0;
    private Map<String, RawMeasurement> mMeasurements;
    private CopyOnWriteArrayList<VehicleDataSink> mSinks;
    private CopyOnWriteArrayList<VehicleDataSource> mSources;

    public DataPipeline() {
        mMeasurements = new HashMap<String, RawMeasurement>();
        mSinks = new CopyOnWriteArrayList<VehicleDataSink>();
        mSources = new CopyOnWriteArrayList<VehicleDataSource>();
    }

    /**
     * Accept new values from data sources.
     *
     * This method is required to implement the SourceCallback interface.
     */
    public void receive(String measurementId, Object value, Object event) {
        mMeasurements.put(measurementId,
                RawMeasurement.measurementFromObjects(value, event));
        for(Iterator<VehicleDataSink> i = mSinks.iterator(); i.hasNext();) {
            (i.next()).receive(measurementId, value, event);
        }
        mMessagesReceived++;
    }

    /**
     * Add a new sink to the pipeline.
     *
     * The sink is also given a reference to a map of the currently known values
     * for all measurements using the
     * {@link VehicleDataSink#setMeasuremrents(Map<String, RawMeasurement>)}
     * method.
     */
    public VehicleDataSink addSink(VehicleDataSink sink) {
        sink.setMeasurements(mMeasurements);
        mSinks.add(sink);
        return sink;
    }

    /**
     * Remove a previously added sink from the pipeline.
     *
     * Once removed, the sink will no longer receive any new measurments from
     * the pipeline's sources. The sink's {@link VehicleDataSink#stop()} method
     * is also called.
     *
     * @param sink if the value is null, it is ignored.
     */
    public void removeSink(VehicleDataSink sink) {
        if(sink != null) {
            mSinks.remove(sink);
            sink.stop();
        }
    }

    /**
     * Add a new source to the pipeline.
     *
     * The source is given a reference to this DataPipeline as its callback.
     */
    public VehicleDataSource addSource(VehicleDataSource source) {
        source.setCallback(this);
        mSources.add(source);
        return source;
    }

    /**
     * Remove a previously added source from the pipeline.
     *
     * Once removed, the source should no longer use this pipeline for its
     * callback.
     *
     * @param source if the value is null, it is ignored.
     */
    public void removeSource(VehicleDataSource source) {
        if(source != null) {
            mSources.remove(source);
            source.stop();
        }
    }

    /**
     * Clear all sources and sinks from the pipeline and stop all of them.
     */
    public void stop() {
        clearSources();
        clearSinks();
    }

    /**
     * Remove and stop all sources in the pipeline.
     */
    public void clearSources() {
        for(Iterator<VehicleDataSource> i = mSources.iterator(); i.hasNext();) {
            (i.next()).stop();
        }
        mSources.clear();
    }

    /**
     * Remove and stop all sinks in the pipeline.
     */
    public void clearSinks() {
        for(Iterator<VehicleDataSink> i = mSinks.iterator(); i.hasNext();) {
            (i.next()).stop();
        }
        mSinks.clear();
    }

    /**
     * Return the last received value for the measurement if known.
     *
     * @return a RawMeasurement with the last known value, or an empty
     * RawMeasurement if no value has been received.
     */
    public RawMeasurement get(String measurementId) {
        RawMeasurement rawMeasurement = mMeasurements.get(measurementId);
        if(rawMeasurement == null) {
            rawMeasurement = new RawMeasurement();
        }
        return rawMeasurement;
    }

    /**
     * Determine if a type of measurement has yet been received.
     *
     * @return true if any value is known for the measurement ID.
     */
    public boolean containsMeasurement(String measurementId) {
        return mMeasurements.containsKey(measurementId);
    }

    /**
     * @return number of messages received since instantiation.
     */
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
