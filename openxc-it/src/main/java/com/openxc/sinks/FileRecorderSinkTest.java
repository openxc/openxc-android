package com.openxc.sinks;

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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.openxc.util.FileOpener;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class FileRecorderSinkTest extends AndroidTestCase {
    FileRecorderSink sink;
    FileOpener opener;
    StringWriter outputString;

    String measurementId = "measurement";
    String value = "value";
    String event = "event";

    @Before
    public void setUp() throws IOException {
        outputString = new StringWriter();
        opener = new MockFileOpener();
        sink = new FileRecorderSink(opener);
    }

    @SmallTest
    public void testReceiveValueOnly() {
        assertThat(outputString.toString(), not(containsString(measurementId)));
        sink.receive(measurementId, value);
        sink.flush();
        assertThat(outputString.toString(), containsString(measurementId));
        assertThat(outputString.toString(), containsString(value));
    }

    @SmallTest
    public void testReceiveEvented() {
        sink.receive(measurementId, value, event);
        sink.flush();
        assertThat(outputString.toString(), containsString(measurementId));
        assertThat(outputString.toString(), containsString(value));
        assertThat(outputString.toString(), containsString(event));
    }

    @SmallTest
    public void testOutputFormat() throws JSONException {
        sink.receive(measurementId, value);
        sink.flush();

        System.out.println("FOO: " + outputString);
        String[] record = outputString.toString().split(":", 2);
        assertThat(record.length, equalTo(2));

        JSONObject message;
        message = new JSONObject(record[1]);
        assertThat(message.getString("name"), equalTo(measurementId));
        assertThat(message.getString("value"), equalTo(value));
    }

    @SmallTest
    public void testStop() {
        assertTrue(sink.isRunning());
        sink.stop();
        assertFalse(sink.isRunning());
    }

    private class MockFileOpener implements FileOpener {
        public BufferedWriter openForWriting(String path) throws IOException {
            return new BufferedWriter(outputString);
        }
    }
}
