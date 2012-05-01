package com.openxc.remote;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.openxc.remote.sinks.BaseVehicleDataSink;

import com.openxc.remote.sources.SourceCallback;
import com.openxc.remote.sources.VehicleDataSource;

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
    public void testSourceCount() {
        assertTrue(pipeline.getSources().isEmpty());
    }

    @Test
    public void testSinkCount() {
        assertTrue(pipeline.getSinks().isEmpty());
    }

    @Test
    public void testAddSource() {
        pipeline.addSource(source);
        assertThat(pipeline.getSources().size(), equalTo(1));
        assertThat((TestSource) pipeline.getSources().get(0), equalTo(source));
        assertThat(source.callback, notNullValue());
    }

    @Test
    public void testAddSink() {
        pipeline.addSink(sink);
        assertThat(pipeline.getSinks().size(), equalTo(1));
        assertThat((TestSink) pipeline.getSinks().get(0), equalTo(sink));
    }

    @Test
    public void testClearSources() {
        pipeline.addSource(source);
        assertThat(pipeline.getSources().size(), equalTo(1));
        pipeline.clearSources();
        assertTrue(pipeline.getSources().isEmpty());
    }

    @Test
    public void testClearSinks() {
        pipeline.addSink(sink);
        assertThat(pipeline.getSinks().size(), equalTo(1));
        pipeline.clearSinks();
        assertTrue(pipeline.getSinks().isEmpty());
    }

    @Test
    public void testStopClearsPipeline() {
        pipeline.addSink(sink);
        pipeline.addSource(source);
        assertThat(pipeline.getSinks().size(), equalTo(1));
        assertThat(pipeline.getSources().size(), equalTo(1));
        pipeline.stop();
        assertTrue(pipeline.getSinks().isEmpty());
        assertTrue(pipeline.getSources().isEmpty());
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
        assertThat((TestSink) pipeline.getSinks().get(0),
                equalTo(anotherSink));
    }

    @Test
    public void testRemoveSource() {
        pipeline.addSource(source);
        TestSource anotherSource = new TestSource();
        pipeline.addSource(anotherSource);
        pipeline.removeSource(source);
        assertThat((TestSource) pipeline.getSources().get(0),
                equalTo(anotherSource));
    }

    private class TestSource implements VehicleDataSource {
        private SourceCallback callback;

        public void sendTestMessage() {
            assertThat(source.callback, notNullValue());
            callback.receive("message", "value", "event");
        }

        public void setCallback(SourceCallback theCallback) {
            callback = theCallback;
        }

        public void stop() {
        }

        public void run() {}
    }

    private class TestSink extends BaseVehicleDataSink {
        public boolean received = false;

        public void receive(String measurementId, Object value, Object event) {
            received = true;
        }

        public void receive(String measurementId, RawMeasurement measurement) {
        }

        public void stop() {
        }
    }
}
