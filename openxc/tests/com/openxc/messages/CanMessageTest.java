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
public class CanMessageTest {
    CanMessage message;
    int id = 42;
    int bus = 1;
    byte[] data = new byte[8];
    HashMap<String, Object> values;

    @Before
    public void setup() {
        values = new HashMap<>();
        values.put(CanMessage.ID_KEY, id);
        values.put(CanMessage.BUS_KEY, bus);
        values.put(CanMessage.DATA_KEY, data);
        message = new CanMessage(bus, id, data);
    }

    @Test
    public void getIdReturnsId() {
        assertEquals(id, message.getId());
    }

    @Test
    public void getBusReturnsBus() {
        assertEquals(bus, message.getBus());
    }

    @Test
    public void getDataReturnsData() {
        assertArrayEquals(data, message.getData());
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildEmptyValues() throws InvalidMessageFieldsException {
        values = new HashMap<>();
        new CanMessage(values);
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildMissingId() throws InvalidMessageFieldsException {
        values.remove(CanMessage.ID_KEY);
        new CanMessage(values);
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildMissingBus() throws InvalidMessageFieldsException {
        values.remove(CanMessage.BUS_KEY);
        new CanMessage(values);
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildMissingData() throws InvalidMessageFieldsException {
        values.remove(CanMessage.DATA_KEY);
        new CanMessage(values);
    }

    @Test
    public void extractsFieldsFromValues()
            throws InvalidMessageFieldsException {
        message = new CanMessage(values);

        assertThat(message.getId(), equalTo(id));
        assertThat(message.getBus(), equalTo(bus));
        assertArrayEquals(message.getData(), data);

        assertFalse(message.contains(CanMessage.ID_KEY));
        assertFalse(message.contains(CanMessage.BUS_KEY));
        assertFalse(message.contains(CanMessage.DATA_KEY));
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
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
}
