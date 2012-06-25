package com.openxc.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;

import com.openxc.remote.RawMeasurement;

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

    @Override
    public void setUp() throws IOException, DataSinkException {
        outputString = new StringWriter();
        opener = new MockFileOpener();
        sink = new FileRecorderSink(opener);
    }

    @SmallTest
    public void testReceiveValueOnly() throws DataSinkException {
        assertTrue(outputString.toString().indexOf(measurementId) == -1);
        sink.receive(new RawMeasurement(measurementId, value));
        sink.flush();
        assertTrue(outputString.toString().indexOf(measurementId) != -1);
        assertTrue(outputString.toString().indexOf(value) != -1);
    }

    @SmallTest
    public void testReceiveEvented() throws DataSinkException {
        sink.receive(new RawMeasurement(measurementId, value, event));
        sink.flush();
        assertTrue(outputString.toString().indexOf(measurementId) != -1);
        assertTrue(outputString.toString().indexOf(value) != -1);
        assertTrue(outputString.toString().indexOf(event) != -1);
    }

    @SmallTest
    public void testOutputFormat() throws JSONException, DataSinkException {
        sink.receive(new RawMeasurement(measurementId, value));
        sink.flush();

        String[] record = outputString.toString().split(":", 2);
        assertTrue(record.length == 2);

        JSONObject message;
        message = new JSONObject(record[1]);
        assertTrue(message.getString("name").equals(measurementId));
        assertTrue(message.getString("value").equals(value));
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
