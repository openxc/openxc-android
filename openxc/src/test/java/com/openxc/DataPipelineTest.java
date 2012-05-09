package com.openxc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.openxc.remote.RawMeasurement;

import com.openxc.sinks.BaseVehicleDataSink;

import com.openxc.sources.SourceCallback;
import com.openxc.sources.VehicleDataSource;

public class DataPipelineTest {
    DataPipeline pipeline;
    TestSource source;
    TestSink sink;

    @Before
    public void setUp() {
        pipeline = new DataPipeline();
        source = new TestSource();
        sink = new TestSink();
    }

    @Test
    public void testAddSource() {
        pipeline.addSource(source);
        assertThat(source.callback, notNullValue());
    }

    @Test
    public void testAddSink() {
        pipeline.addSource(source);
        source.sendTestMessage();
        assertFalse(sink.received);
        pipeline.addSink(sink);
        source.sendTestMessage();
        assertTrue(sink.received);
    }

    @Test
    public void testClearSources() {
        pipeline.addSource(source);
        pipeline.addSink(sink);
        pipeline.clearSources();
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void testClearSinks() {
        pipeline.addSink(sink);
        pipeline.clearSinks();
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void testStopClearsPipeline() {
        pipeline.addSink(sink);
        pipeline.addSource(source);
        source.sendTestMessage();
        assertTrue(sink.received);
        sink.received = false;
        pipeline.stop();
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void testReceiveNewData() {
        pipeline.addSink(sink);
        pipeline.receive("measurement", "value", "event");
        assertTrue(sink.received);
    }

    @Test
    public void testConnectsSourceCallback() {
        pipeline.addSink(sink);
        pipeline.addSource(source);
        source.sendTestMessage();
        assertTrue(sink.received);
    }

    @Test
    public void testRemoveSink() {
        pipeline.addSink(sink);
        TestSink anotherSink = new TestSink();
        pipeline.addSink(anotherSink);
        pipeline.removeSink(sink);
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void testRemoveSource() {
        pipeline.addSource(source);
        TestSource anotherSource = new TestSource();
        pipeline.addSource(anotherSource);
        pipeline.removeSource(source);
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    private class TestSource implements VehicleDataSource {
        private SourceCallback callback;

        public void sendTestMessage() {
            if(callback != null) {
                callback.receive("message", "value", "event");
            }
        }

        public void setCallback(SourceCallback theCallback) {
            callback = theCallback;
        }

        public void stop() {
            callback = null;
        }
    }

    private class TestSink extends BaseVehicleDataSink {
        public boolean received = false;

        public void receive(String measurementId, Object value, Object event) {
            received = true;
        }

        public void receive(String measurementId, RawMeasurement measurement) {
        }
    }
}
