package com.openxc.messages;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void valueAsNumber() {
        assertEquals(message.getValueAsNumber(), value);
    }

    @Test
    public void valueAsString() {
        message = new SimpleVehicleMessage(name, "foo");
        assertEquals("foo" ,message.getValueAsString());
    }

    @Test
    public void valueAsBoolean() {
        message = new SimpleVehicleMessage(name, Boolean.valueOf(true));
        assertEquals(true, message.getValueAsBoolean());
    }

    @Test
    public void sameNameAndValueEquals() {
        SimpleVehicleMessage anotherMessage = new SimpleVehicleMessage(
                message.getTimestamp(), name, value);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void differentValueDoesntEqual() {
        NamedVehicleMessage anotherMessage = new SimpleVehicleMessage(
                name, Double.valueOf(24));
        assertFalse(message.equals(anotherMessage));
    }

    @Test
    public void toStringNotNull() {
        assertThat(message.toString(), notNullValue());
    }

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(SimpleVehicleMessage.class));
        assertEquals(message, createdFromParcel);
    }

    @Test
    public void keyNotNull() {
        assertThat(message.getKey(), notNullValue());
    }
}
