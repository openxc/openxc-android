package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class VehicleMessageTest {
    VehicleMessage message;
    HashMap<String, Object> extras;
    String key = "foo";

    @Before
    public void setup()  {
        extras = new HashMap<String, Object>();
        extras.put(key, Integer.valueOf(42));
        message = new VehicleMessage(extras);
    }

    @Test
    public void hasExtraValue() {
        assertEquals(42, message.getExtras().get(key));
    }

    @Test
    public void emptyMessageHasNoTimestamp() {
        message = new VehicleMessage();
        assertTrue(message.getExtras() != null);
        assertTrue(!message.isTimestamped());
    }

    @Test
    public void emptyMessage() {
        message = new VehicleMessage(new HashMap<String, Object>());
        assertTrue(message.getExtras() != null);
    }

    @Test
    public void setManualTimestamp() {
        message = new VehicleMessage(Long.valueOf(10000), extras);
        assertTrue(message.isTimestamped());
        assertEquals(Long.valueOf(10000), message.getTimestamp());
    }

    @Test
    public void setAutomaticTriggeredTimestamp() {
        message = new VehicleMessage();
        message.timestamp();
        assertTrue(message.isTimestamped());
        Date date = new Date(message.getTimestamp());
        // Catch a regression where we didn't divide by 1000 before storing
        // timestamp as a double.
        assertThat(date.getYear(), equalTo(new Date().getYear()));
    }

    @Test
    public void untimestamp() {
        message = new VehicleMessage(Long.valueOf(10000), extras);
        message.untimestamp();
        assertFalse(message.isTimestamped());
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void sameValuesEquals() {
        VehicleMessage anotherMessage = new VehicleMessage(
                message.getTimestamp(), extras);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void differentTimestampNotEqual() {
        VehicleMessage anotherMessage = new VehicleMessage(
                Long.valueOf(10000), extras);
        assertFalse(message.equals(anotherMessage));
    }

    @Test
    public void differentValuesNotEqual() {
        extras.put("another", "foo");
        // This also tests that the values map is copied and we don't have the
        // same reference from outside the class
        VehicleMessage anotherMessage = new VehicleMessage(
                message.getTimestamp(), extras);
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
        assertEquals(message, createdFromParcel);
    }
}
