package com.openxc.messages;

import java.util.HashMap;

import junit.framework.TestCase;

import android.os.Parcel;

public class VehicleMessageTest extends TestCase {
    VehicleMessage message;
    HashMap<String, Object> data;

    public void setUp() {
        data = new HashMap<String, Object>();
        data.put("value", Double.valueOf(42));
        message = new VehicleMessage(data);
    }

    public void testTimestamp() {
        assertTrue(message.isTimestamped());
        assertTrue(message.getTimestamp() > 0);
    }

    public void testValue() {
        assertEquals(42, message.get("value"));
    }

    public void testEmptyMessage() {
        message = new VehicleMessage(new HashMap<String, Object>());
    }

    public void testTimestampExtractedFromValues() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("value", Double.valueOf(42));
        data.put(VehicleMessage.TIMESTAMP_KEY, Long.valueOf(1000));
        message = new VehicleMessage(data);

        assertEquals(42, message.get("value"));
        assertFalse(message.contains(VehicleMessage.TIMESTAMP_KEY));
        assertEquals(null, message.get("value"));
        assertEquals(1000, message.getTimestamp());
    }

    public void testSetManualTimestamp() {
        message = new VehicleMessage(Long.valueOf(10000), data);
        assertTrue(message.isTimestamped());
        assertFalse(10000 == message.getTimestamp());
    }

    public void testUntimestamp() {
        message = new VehicleMessage(Long.valueOf(10000), data);
        message.untimestamp();
        assertFalse(message.isTimestamped());
    }

    public void testSameEquals() {
        assertEquals(message, message);
    }

    public void testSameValuesEquals() {
        VehicleMessage anotherMessage = new VehicleMessage(
                message.getTimestamp(), data);
        assertEquals(message, anotherMessage);
    }

    public void testDifferentTimestampNotEqual() {
        VehicleMessage anotherMessage = new VehicleMessage(data);
        assertFalse(message.equals(anotherMessage));
    }

    public void testDifferentValuesNotEqual() {
        data.put("another", "foo");
        VehicleMessage anotherMessage = new VehicleMessage(
                message.getTimestamp(), data);
        assertFalse(message.equals(anotherMessage));
    }

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
