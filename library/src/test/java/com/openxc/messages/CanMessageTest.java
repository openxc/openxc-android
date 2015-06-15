package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@RunWith(RobolectricTestRunner.class)
public class CanMessageTest {
    CanMessage message;
    int id = 42;
    int bus = 1;
    byte[] data = new byte[] {1,2,3,4,5,6,7,8};

    @Before
    public void setup() {
        message = new CanMessage(bus, id, data);
    }

    @Test
    public void getIdReturnsId() {
        assertEquals(id, message.getId());
    }

    @Test
    public void getBusReturnsBus() {
        assertEquals(bus, message.getBusId());
    }

    @Test
    public void getDataReturnsData() {
        assertArrayEquals(data, message.getData());
    }

    @Test
    public void setNoData() {
        message = new CanMessage(bus, id, null);
        assertThat(message.getData(), notNullValue());
        assertArrayEquals(new byte[]{0,0,0,0,0,0,0,0}, message.getData());
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void nullNotEqual() {
        assertThat(message, not(equalTo(null)));
    }

    @Test
    public void differentIdNotEqual() {
        CanMessage anotherMessage = new CanMessage(id + 1, bus, data);
        assertThat(message, not(equalTo(anotherMessage)));
    }

    @Test
    public void differentBusNotEqual() {
        CanMessage anotherMessage = new CanMessage(id, bus + 1, data);
        assertThat(message, not(equalTo(anotherMessage)));
    }

    @Test
    public void differentDataNotEqual() {
        CanMessage anotherMessage = new CanMessage(id, bus,
                new byte[] {8,7,6,5,4,3,2,1});
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
        assertThat(createdFromParcel, instanceOf(CanMessage.class));
        assertEquals(message, createdFromParcel);
    }

    @Test
    public void keyNotNull() {
        assertThat(message.getKey(), notNullValue());
    }
}
