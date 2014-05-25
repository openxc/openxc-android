package com.openxc.messages;

import java.util.HashMap;

import junit.framework.TestCase;

import android.os.Parcel;

public class NamedVehicleMessageTest extends TestCase {
    NamedVehicleMessage message;
    HashMap<String, Object> data;

    public void setUp() {
        data = new HashMap<String, Object>();
        data.put("value", Double.valueOf(42));
        message = new NamedVehicleMessage("foo", data);
    }

    public void testName() {
        assertEquals("foo", message.getName());
    }

    public void testExtractsNameFromValues() {
        data.put("name", "bar");
        message = new NamedVehicleMessage(data);
        assertEquals("bar", message.getName());
        assertFalse(message.contains("name"));
    }

    public void testWriteAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertTrue(message instanceof NamedVehicleMessage);
        assertEquals(message, createdFromParcel);
    }
}
