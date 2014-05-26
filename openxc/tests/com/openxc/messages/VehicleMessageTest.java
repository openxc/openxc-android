package com.openxc.messages;

import java.util.HashMap;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class VehicleMessageTest extends TestCase {
    VehicleMessage message;
    HashMap<String, Object> data;

    @Before
    public void setup() {
        data = new HashMap<String, Object>();
        data.put("value", Integer.valueOf(42));
        message = new VehicleMessage(data);
    }

    @Test
    public void testTimestamp() {
        assertTrue(message.isTimestamped());
        assertTrue(message.getTimestamp() > 0);
    }

    @Test
    public void testValue() {
        assertEquals(42, message.get("value"));
    }

    @Test
    public void testEmptyMessage() {
        message = new VehicleMessage(new HashMap<String, Object>());
        assertTrue(message.getValuesMap() != null);
    }

    @Test
    public void testTimestampExtractedFromValues() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("value", Integer.valueOf(42));
        data.put(VehicleMessage.TIMESTAMP_KEY, Long.valueOf(1000));
        message = new VehicleMessage(data);

        assertEquals(42, message.get("value"));
        assertFalse(message.contains(VehicleMessage.TIMESTAMP_KEY));
        assertEquals(null, message.get(VehicleMessage.TIMESTAMP_KEY));
        assertEquals(1000, message.getTimestamp());
    }

    @Test
    public void testSetManualTimestamp() {
        message = new VehicleMessage(Long.valueOf(10000), data);
        assertTrue(message.isTimestamped());
        assertEquals(10000, message.getTimestamp());
    }

    @Test
    public void testUntimestamp() {
        message = new VehicleMessage(Long.valueOf(10000), data);
        message.untimestamp();
        assertFalse(message.isTimestamped());
    }

    @Test
    public void testSameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void testSameValuesEquals() {
        VehicleMessage anotherMessage = new VehicleMessage(
                message.getTimestamp(), data);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void testDifferentTimestampNotEqual() {
        VehicleMessage anotherMessage = new VehicleMessage(Long.valueOf(10000), data);
        assertFalse(message.equals(anotherMessage));
    }

    @Test
    public void testDifferentValuesNotEqual() {
        data.put("another", "foo");
        // This also tests that the values map is copied and we don't have the
        // same reference from outside the class
        VehicleMessage anotherMessage = new VehicleMessage(
                message.getTimestamp(), data);
        assertFalse(message.equals(anotherMessage));
    }

    @Test
    public void testWriteAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertEquals(message, createdFromParcel);
    }
}
