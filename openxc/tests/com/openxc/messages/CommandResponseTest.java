package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import android.os.Parcel;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CommandResponseTest {
    CommandResponse response;
    String message = "bar";
    String command = "foo";

    @Before
    public void setup() {
        response = new CommandResponse(command, message);
    }

    @Test
    public void getCommandReturnsCommand() {
        assertEquals(command, response.getCommand());
    }

    @Test
    public void getMessageReturnsMessage() {
        assertEquals(command, response.getCommand());
    }

    @Test
    public void constructWithExtraValuesHasMessage() {
        response = new CommandResponse(command, message,
                new HashMap<String, Object>());
        assertEquals(message, response.getMessage());
    }

    @Test
    public void sameEquals() {
        assertEquals(response, response);
    }

    @Test
    public void sameCommandAndMessageEquals() {
        CommandResponse anotherResponse = new CommandResponse(
                command, message);
        assertEquals(response, anotherResponse);
    }

    @Test
    public void differentMessageDoesntEqual() {
        CommandResponse anotherResponse = new CommandResponse(
                command, message + " different");
        assertFalse(response.equals(anotherResponse));
    }

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        response.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(CommandResponse.class));
        assertEquals(response, createdFromParcel);
    }
}
