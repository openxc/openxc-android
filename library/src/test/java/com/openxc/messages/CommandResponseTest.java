package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

import com.openxc.messages.Command.CommandType;

@RunWith(RobolectricTestRunner.class)
public class CommandResponseTest {
    CommandResponse response;
    String message = "bar";
    boolean status = true;
    CommandType command = CommandType.VERSION;

    @Before
    public void setup() {
        response = new CommandResponse(command, status, message);
    }

    @Test
    public void getCommandReturnsCommand() {
        assertEquals(command, response.getCommand());
    }

    @Test
    public void getStatusReturnsStatus() {
        assertEquals(status, response.getStatus());
    }

    @Test
    public void getMessageReturnsMessage() {
        assertEquals(command, response.getCommand());
    }

    @Test
    public void sameEquals() {
        assertEquals(response, response);
    }

    @Test
    public void sameCommandAndMessageEquals() {
        CommandResponse anotherResponse = new CommandResponse(
                command, status, message);
        assertEquals(response, anotherResponse);
    }

    @Test
    public void differentMessageDoesntEqual() {
        CommandResponse anotherResponse = new CommandResponse(
                command, status, message + " different");
        assertFalse(response.equals(anotherResponse));
    }

    @Test
    public void differentStatusDoesntEqual() {
        CommandResponse anotherResponse = new CommandResponse(
                command, !status, message);
        assertFalse(response.equals(anotherResponse));
    }

    @Test
    public void differentCommandDoesntEqual() {
        CommandResponse anotherResponse = new CommandResponse(
                CommandType.DEVICE_ID, status);
        assertFalse(response.equals(anotherResponse));
    }

    @Test
    public void toStringNotNull() {
        assertThat(response.toString(), notNullValue());
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

    @Test
    public void keyNotNull() {
        assertThat(response.getKey(), notNullValue());
    }

    @Test
    public void matchesKeyFromCommand() {
        Command originalCommand = new Command(command);
        KeyMatcher matcher = ExactKeyMatcher.buildExactMatcher(originalCommand);
        assertTrue(matcher.matches(response));
    }
}
