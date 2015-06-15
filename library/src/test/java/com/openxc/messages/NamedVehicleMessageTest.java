package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@RunWith(RobolectricTestRunner.class)
public class NamedVehicleMessageTest {
    NamedVehicleMessage message;

    @Before
    public void setup() {
        message = new NamedVehicleMessage("foo");
    }

    @Test
    public void getNameReturnsName() {
        assertEquals("foo", message.getName());
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void genericAsNamed() {
        VehicleMessage generic = message;
        assertEquals(generic.asNamedMessage(), message);
    }

    @Test
    public void differentNameDoesntEqual() {
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("bar");
        assertThat(message, not(equalTo(anotherMessage)));
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
        assertThat(createdFromParcel, instanceOf(NamedVehicleMessage.class));
        assertEquals(message, createdFromParcel);
    }

    @Test
    public void keyMatches() {
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("foo");
        assertThat(message.getKey(), equalTo(anotherMessage.getKey()));
    }

    @Test
    public void keyNotNull() {
        assertThat(message.getKey(), notNullValue());
    }
}
