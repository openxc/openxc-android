package com.openxc.messages;

import java.util.HashMap;

import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class SimpleVehicleMessageTest {
    SimpleVehicleMessage message;
    Double value = Double.valueOf(42);
    String name = "foo";

    @Before
    public void setup() {
        message = new SimpleVehicleMessage(name, value);
    }

    @Test
    public void getNameReturnsName() {
        assertEquals(name, message.getName());
    }

    @Test
    public void extractsNameAndValueFromValues() {
        HashMap<String, Object> data = new HashMap<>();
        data.put(NamedVehicleMessage.NAME_KEY, name);
        data.put(SimpleVehicleMessage.VALUE_KEY, value);
        message = new SimpleVehicleMessage(data);
        assertEquals(name, message.getName());
        assertEquals(value, message.getValue());
        assertFalse(message.contains(NamedVehicleMessage.NAME_KEY));
        assertFalse(message.contains(SimpleVehicleMessage.VALUE_KEY));
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void sameNameAndValueEquals() {
        SimpleVehicleMessage anotherMessage = new SimpleVehicleMessage(
                name, value);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void differentValueDoesntEqual() {
        NamedVehicleMessage anotherMessage = new SimpleVehicleMessage(
                name, Double.valueOf(24));
        assertFalse(message.equals(anotherMessage));
    }

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertTrue(createdFromParcel instanceof SimpleVehicleMessage);
        assertEquals(message, createdFromParcel);
    }
}
