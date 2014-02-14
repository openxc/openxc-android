package com.openxc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.remote.RawMeasurement;
import com.openxc.sinks.DataSinkException;
import com.openxc.sinks.VehicleDataSink;
import com.openxc.sources.SourceCallback;
import com.openxc.sources.VehicleDataSource;

/**
 * A pipeline that ferries data from VehicleDataSources to VehicleDataSinks.
 *
 * A DataPipeline accepts two types of components - sources and sinks. The
 * sources (implementing {@link VehicleDataSource} call the
 * {@link #receive(RawMeasurement)} method on the this class when new
 * values arrive. The DataPipeline then passes this value on to all currently
 * registered data sinks.
 *
 * The Pipeline can have an optional Operator, which implements a few callbacks
 * to check the status of the pipeline - e.g. if some source in the pipeline is
 * active.
 */
public class DataPipeline implements SourceCallback {
    private static final String TAG = "DataPipeline";

    private Operator mOperator;
    private int mMessagesReceived = 0;
    private Map<String, RawMeasurement> mMeasurements =
            new ConcurrentHashMap<String, RawMeasurement>();
    private CopyOnWriteArrayList<VehicleDataSink> mSinks =
            new CopyOnWriteArrayList<VehicleDataSink>();
    private CopyOnWriteArrayList<VehicleDataSource> mSources =
            new CopyOnWriteArrayList<VehicleDataSource>();

    public interface Operator {
        public void onPipelineDeactivated();
        public void onPipelineActivated();
    }

    public DataPipeline() {
        this(null);
    }

    public DataPipeline(Operator operator) {
        mOperator = operator;
    }

    /**
     * Accept new values from data sources and send it out to all registered
     * sinks.
     *
     * This method is required to implement the SourceCallback interface.
     *
     * If any data sink throws a DataSinkException when receiving data, it will
     * be removed from the list of sinks.
     */
    public void receive(RawMeasurement measurement) {
        if(measurement == null) {
            return;
        }

        if(measurement.getName() == null) {
            Log.d(TAG, "Measurement's name was null - that shouldn't have made it this far");
            return;
        }

        mMeasurements.put(measurement.getName(), measurement);
        List<VehicleDataSink> deadSinks = new ArrayList<VehicleDataSink>();
        for(Iterator<VehicleDataSink> i = mSinks.iterator(); i.hasNext();) {
            VehicleDataSink sink = i.next();
            try {
                sink.receive(measurement);
            } catch(DataSinkException e) {
                Log.w(TAG, this.getClass().getName() + ": The sink " +
                        sink + " exploded when we sent a new message " +
                        "-- removing it from the pipeline: " + e);
                deadSinks.add(sink);
            }
        }
        mMessagesReceived++;
        for(VehicleDataSink sink : deadSinks) {
            removeSink(sink);
        }
    }

    /**
     * Add a new sink to the pipeline.
     */
    public VehicleDataSink addSink(VehicleDataSink sink) {
        mSinks.add(sink);
        return sink;
    }

    /**
     * Remove a previously added sink from the pipeline.
     *
     * Once removed, the sink will no longer receive any new measurements from
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

    public List<VehicleDataSource> getSources() {
        return mSources;
    }

    public List<VehicleDataSink> getSinks() {
        return mSinks;
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
     * @return a RawMeasurement with the last known value, or null if no value
     *          has been received.
     */
    public RawMeasurement get(String measurementId) {
        return mMeasurements.get(measurementId);
    }

    /**
     * @return number of messages received since instantiation.
     */
    public int getMessageCount() {
        return mMessagesReceived;
    }

    public boolean isActive() {
        return isActive(null);
    }

    /**
     * Return true if at least one source is active.
     */
    public boolean isActive(VehicleDataSource skipSource) {
        boolean connected = false;
        for(VehicleDataSource s : mSources) {
            if(s != skipSource) {
                connected = connected || s.isConnected();
            }
        }
        return connected;
    }

    /**
     * At least one source is not active - if all sources are inactive, notify
     * the operator.
     */
    public void sourceDisconnected(VehicleDataSource source) {
        if(mOperator != null) {
            if(!isActive(source)) {
                mOperator.onPipelineDeactivated();
            }
        }
    }

    /**
     * At least one source is active - notify the operator.
     */
    public void sourceConnected(VehicleDataSource source) {
        if(mOperator != null) {
            mOperator.onPipelineActivated();
        }
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
