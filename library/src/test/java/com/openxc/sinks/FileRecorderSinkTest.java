package com.openxc.sinks;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;

import com.openxc.messages.KeyedMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.JsonFormatter;
import com.openxc.util.FileOpener;

@RunWith(RobolectricTestRunner.class)
public class FileRecorderSinkTest {
    FileRecorderSink sink;
    FileOpener opener;
    StringWriter outputString;

    String measurementId = "measurement";
    String value = "value";

    @Before
    public void setUp() throws IOException, DataSinkException {
        outputString = new StringWriter();
        opener = new MockFileOpener();
        sink = new FileRecorderSink(opener);
    }

    @Test
    public void testReceiveValueOnly() throws DataSinkException {
        assertTrue(outputString.toString().indexOf(measurementId) == -1);
        sink.receive(new SimpleVehicleMessage(measurementId, value));
        sink.flush();
        assertTrue(outputString.toString().indexOf(measurementId) != -1);
        assertTrue(outputString.toString().indexOf(value) != -1);
    }

    @Test
    public void testOutputFormat() throws JSONException, DataSinkException {
        VehicleMessage measurement = new SimpleVehicleMessage(measurementId, value);
        sink.receive(measurement);
        sink.flush();

        JSONObject message;
        message = new JSONObject(outputString.toString());
        assertTrue(message.getString("name").equals(measurementId));
        assertTrue(message.getString("value").equals(value));
    }

    @Test
    public void testSerialize() throws DataSinkException {
        KeyedMessage measurement = new SimpleVehicleMessage(measurementId, value);
        measurement.asKeyedMessage().getKey();
        String serializedMessage = JsonFormatter.serialize(measurement);
        assertTrue(!serializedMessage.contains("mKey"));
    }

    @Test
    public void testCounts() throws JSONException, DataSinkException {
        VehicleMessage measurement = new SimpleVehicleMessage("first", true);
        sink.receive(measurement);

        measurement = new SimpleVehicleMessage("first", false);
        sink.receive(measurement);

        measurement = new SimpleVehicleMessage("second", true);
        sink.receive(measurement);

        measurement = new SimpleVehicleMessage("second", true);
        sink.receive(measurement);

        sink.flush();

        String[] records = outputString.toString().split("\n");
        assertEquals(4, records.length);
        assertTrue(records[0].indexOf("first") != -1);
        assertTrue(records[1].indexOf("first") != -1);
        assertTrue(records[2].indexOf("second") != -1);
        assertTrue(records[3].indexOf("second") != -1);
    }

    @Test
    public void testStop() throws DataSinkException {
        VehicleMessage measurement = new SimpleVehicleMessage("first", true);
        sink.receive(measurement);

        measurement = new SimpleVehicleMessage("second", false);
        sink.receive(measurement);
        sink.stop();

        measurement = new SimpleVehicleMessage("third", true);
        try {
            sink.receive(measurement);
            fail("Expected a DataSinkException");
        } catch(DataSinkException e) {
        }

        String[] records = outputString.toString().split("\n");
        assertEquals(2, records.length);
        assertTrue(records[0].indexOf("first") != -1);
        assertTrue(records[1].indexOf("second") != -1);
    }

    private class MockFileOpener implements FileOpener {
        @Override
        public BufferedWriter openForWriting(String path) throws IOException {
            return new BufferedWriter(outputString);
        }
    }
}
