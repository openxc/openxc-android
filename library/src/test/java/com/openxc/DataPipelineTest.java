package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.openxc.messages.KeyedMessage;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.sources.TestSource;
import com.openxc.sinks.TestSink;

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
        KeyedMessage message = new SimpleVehicleMessage(name, value);
        pipeline.receive(message);
        SimpleVehicleMessage retreived = (SimpleVehicleMessage) pipeline.get(
                message.getKey());
        assertThat(retreived, notNullValue());
        assertThat((String)retreived.getValue(), equalTo(value));
    }

    @Test
    public void getKeyed() {
        DiagnosticRequest request = new DiagnosticRequest(42, 1, 2, 4);
        pipeline.receive(request);
        VehicleMessage message = pipeline.get(request.getKey());
        assertThat(message, notNullValue());
        assertEquals(message, request);
    }
}
