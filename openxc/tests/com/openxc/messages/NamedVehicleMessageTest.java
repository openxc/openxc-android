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
public class NamedVehicleMessageTest {
    NamedVehicleMessage message;
    HashMap<String, Object> data;

    @Before
    public void setup() {
        data = new HashMap<String, Object>();
        data.put("value", Double.valueOf(42));
        message = new NamedVehicleMessage("foo", data);
    }

    @Test
    public void testName() {
        assertEquals("foo", message.getName());
    }

    @Test
    public void getValues() {
        Double value = (Double) message.get("value");
        assertThat(value, notNullValue());
        assertEquals(value.doubleValue(), 42, 0);
    }

    @Test
    public void testExtractsNameFromValues() {
        data.put(NamedVehicleMessage.NAME_KEY, "bar");
        message = new NamedVehicleMessage(data);
        assertEquals("bar", message.getName());
        assertFalse(message.contains(NamedVehicleMessage.NAME_KEY));
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void sameNameAndValueEquals() {
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("foo", data);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void sameNameDifferentValueDoesntEqual() {
        data = new HashMap<String, Object>();
        data.put("value", Double.valueOf(24));
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("foo", data);
        assertThat(message, not(equalTo(anotherMessage)));
    }

    @Test
    public void differentNameDoesntEqual() {
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("bar", data);
        assertThat(message, not(equalTo(anotherMessage)));
    }

    @Test
    public void testWriteAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertTrue(createdFromParcel instanceof NamedVehicleMessage);
        assertEquals(message, createdFromParcel);
    }

    @Test
    public void keyMatches() {
        NamedVehicleMessage anotherMessage = new NamedVehicleMessage("foo", data);
        assertThat(message.getKey(), equalTo(anotherMessage.getKey()));
    }
}
