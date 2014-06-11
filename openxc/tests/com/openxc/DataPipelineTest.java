package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import junit.framework.TestCase;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.HashMap;

import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.InvalidMessageFieldsException;
import com.openxc.sinks.BaseVehicleDataSink;
import com.openxc.sources.BaseVehicleDataSource;
import com.openxc.sources.SourceCallback;

public class DataPipelineTest {
    DataPipeline pipeline;
    TestSource source;
    TestSink sink;

    @Before
    public void set() {
        pipeline = new DataPipeline();
        source = new TestSource();
        sink = new TestSink();
    }

    @Test
    public void addSource() {
        pipeline.addSource(source);
        assertThat(source.callback, notNullValue());
    }

    @Test
    public void addSink() {
        pipeline.addSource(source);
        source.sendTestMessage();
        assertFalse(sink.received);
        pipeline.addSink(sink);
        source.sendTestMessage();
        assertTrue(sink.received);
    }

    @Test
    public void clearSources() {
        pipeline.addSource(source);
        pipeline.addSink(sink);
        pipeline.clearSources();
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void clearSinks() {
        pipeline.addSink(sink);
        pipeline.clearSinks();
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void stopClearsPipeline() {
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
    public void receiveNewData() {
        pipeline.addSink(sink);
        pipeline.receive(new SimpleVehicleMessage("measurement", "value"));
        assertTrue(sink.received);
    }

    @Test
    public void connectsSourceCallback() {
        pipeline.addSink(sink);
        pipeline.addSource(source);
        source.sendTestMessage();
        assertTrue(sink.received);
    }

    @Test
    public void removeSink() {
        pipeline.addSink(sink);
        TestSink anotherSink = new TestSink();
        pipeline.addSink(anotherSink);
        pipeline.removeSink(sink);
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void removeSource() {
        pipeline.addSource(source);
        TestSource anotherSource = new TestSource();
        pipeline.addSource(anotherSource);
        pipeline.removeSource(source);
        source.sendTestMessage();
        assertFalse(sink.received);
    }

    @Test
    public void getNamed() {
        String name = "foo";
        String value = "value";
        pipeline.receive(new SimpleVehicleMessage(name, value));
        SimpleVehicleMessage message = (SimpleVehicleMessage) pipeline.get(name);
        assertThat(message, notNullValue());
        assertThat((String)message.getValue(), equalTo(value));
    }

    @Test
    public void getUnnamed() throws InvalidMessageFieldsException {
        HashMap<String, Object> data = new HashMap<>();
        pipeline.receive(new VehicleMessage(data));
        NamedVehicleMessage message = pipeline.get("foo");
        assertThat(message, nullValue());
    }

    private class TestSource extends BaseVehicleDataSource {
        private SourceCallback callback;

        public void sendTestMessage() {
            if(callback != null) {
                callback.receive(new SimpleVehicleMessage("message", "value"));
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

        public boolean receive(VehicleMessage measurement) {
            received = true;
            return true;
        }
    }
}
