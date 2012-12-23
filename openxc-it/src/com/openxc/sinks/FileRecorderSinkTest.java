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
        RawMeasurement measurement = new RawMeasurement(measurementId, value);
        sink.receive(measurement);
        sink.flush();

        JSONObject message;
        message = new JSONObject(outputString.toString());
        assertTrue(message.getString("name").equals(measurementId));
        assertTrue(message.getString("value").equals(value));
    }

    @SmallTest
    public void testCounts() throws JSONException, DataSinkException {
        RawMeasurement measurement = new RawMeasurement("first", true);
        sink.receive(measurement);

        measurement = new RawMeasurement("first", false);
        sink.receive(measurement);

        measurement = new RawMeasurement("second", true);
        sink.receive(measurement);

        measurement = new RawMeasurement("second", true);
        sink.receive(measurement);

        sink.flush();

        String[] records = outputString.toString().split("\n");
        assertEquals(4, records.length);
        assertTrue(records[0].indexOf("first") != -1);
        assertTrue(records[1].indexOf("first") != -1);
        assertTrue(records[2].indexOf("second") != -1);
        assertTrue(records[3].indexOf("second") != -1);
    }

    @SmallTest
    public void testStop() throws DataSinkException {
        RawMeasurement measurement = new RawMeasurement("first", true);
        assertTrue(sink.receive(measurement));

        measurement = new RawMeasurement("second", false);
        assertTrue(sink.receive(measurement));
        sink.stop();

        measurement = new RawMeasurement("third", true);
        assertFalse(sink.receive(measurement));

        String[] records = outputString.toString().split("\n");
        assertEquals(2, records.length);
        assertTrue(records[0].indexOf("first") != -1);
        assertTrue(records[1].indexOf("second") != -1);
    }

    private class MockFileOpener implements FileOpener {
        public BufferedWriter openForWriting(String path) throws IOException {
            return new BufferedWriter(outputString);
        }
    }
}
