package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@Config(emulateSdk = 18, manifest = Config.NONE)
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

    // TODO test storing extra values, comparing with values

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void differentNameDoesntEqual() {
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("bar");
        assertThat(message, not(equalTo(anotherMessage)));
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
    public void keyMatches() throws InvalidMessageFieldsException {
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("foo");
        assertThat(message.getKey(), equalTo(anotherMessage.getKey()));
    }
}
