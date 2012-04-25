package com.openxc.remote.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.openxc.remote.RawMeasurement;

import com.openxc.util.FileOpener;

import android.util.Log;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    Log.class
})
public class FileRecorderSinkTest {
    FileRecorderSink sink;
    FileOpener opener;
    StringWriter outputString;

    String measurementId = "measurement";
    String value = "value";
    String event = "event";

    @Before
    public void setUp() throws IOException {
        mockLogger();
        outputString = new StringWriter();
        opener = new MockFileOpener();
        sink = new FileRecorderSink(opener);
    }

    @Test
    public void testReceiveValueOnly() {
        assertThat(outputString.toString(), not(containsString(measurementId)));
        sink.receive(measurementId, value);
        sink.flush();
        assertThat(outputString.toString(), containsString(measurementId));
        assertThat(outputString.toString(), containsString(value));
    }

    @Test
    public void testReceiveEvented() {
        sink.receive(measurementId, value, event);
        sink.flush();
        assertThat(outputString.toString(), containsString(measurementId));
        assertThat(outputString.toString(), containsString(value));
        assertThat(outputString.toString(), containsString(event));
    }

    @Test
    public void testReceiveRawMeasurement() {
        sink.receive(measurementId, new RawMeasurement(value));
        sink.flush();
        assertThat(outputString.toString(), containsString(measurementId));
        assertThat(outputString.toString(), containsString(value));
    }

    @Test
    public void testValidJson() throws JSONException {
        sink.receive(measurementId, new RawMeasurement(value));

        JSONObject message;
        message = new JSONObject(outputString.toString());
        assertThat(message.getString("name"), equalTo(measurementId));
        assertThat(message.getString("value"), equalTo(value));
    }

    @Test
    public void testStop() {
        assertTrue(sink.isRunning());
        sink.stop();
        assertFalse(sink.isRunning());
    }

    private void mockLogger() {
        PowerMockito.mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenAnswer(new Answer<Integer>()
        {

            @Override
            public Integer answer(final InvocationOnMock invocation) throws Throwable
            {
                final String tag = (String) invocation.getArguments()[0];
                final String msg = (String) invocation.getArguments()[1];
                System.out.println("[" + tag + "] " + msg);
                return 0;
            }
        });

    }

    private class MockFileOpener implements FileOpener {
        public BufferedWriter openForWriting(String path) throws IOException {
            return new BufferedWriter(outputString);
        }
    }
}
