package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@RunWith(RobolectricTestRunner.class)
public class EventedSimpleVehicleMessageTest {
    EventedSimpleVehicleMessage message;
    Double value = Double.valueOf(42);
    Double event = Double.valueOf(21);
    String name = "foo";

    @Before
    public void setup() {
        message = new EventedSimpleVehicleMessage(name, value, event);
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void sameEventEquals() {
        EventedSimpleVehicleMessage anotherMessage = new EventedSimpleVehicleMessage(
                message.getTimestamp(), name, value, event);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void differentEventDoesntEqual() {
        NamedVehicleMessage anotherMessage = new EventedSimpleVehicleMessage(
                name, Double.valueOf(24), Double.valueOf(444.1));
        assertFalse(message.equals(anotherMessage));
    }

    @Test
    public void toStringNotNull() {
        assertThat(message.toString(), notNullValue());
    }

    @Test
    public void eventAsNumber() {
        assertEquals(message.getEventAsNumber(), event);
    }

    @Test
    public void eventAsString() {
        message = new EventedSimpleVehicleMessage(name, value, "foo");
        assertEquals(message.getEventAsString(), "foo");
    }

    @Test
    public void eventAsBoolean() {
        message = new EventedSimpleVehicleMessage(name, value,
                Boolean.valueOf(true));
        assertEquals(message.getEventAsBoolean(), true);
    }

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(EventedSimpleVehicleMessage.class));
        assertEquals(message, createdFromParcel);
    }

    @Test
    public void keyNotNull() {
        assertThat(message.getKey(), notNullValue());
    }
}
