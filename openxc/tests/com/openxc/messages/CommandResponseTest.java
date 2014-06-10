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
    public void messageIsOptional() {
        HashMap<String, Object> data = new HashMap<>();
        data.put(CommandResponse.COMMAND_RESPONSE_KEY, command);
        response = new CommandResponse(data);
        assertThat(response.getCommand(), equalTo(command));
        assertThat(response.getMessage(), nullValue());
        assertFalse(response.contains(CommandResponse.COMMAND_RESPONSE_KEY));
    }

    @Test
    public void extractsCommandAndMessageFromValues() {
        HashMap<String, Object> data = new HashMap<>();
        data.put(CommandResponse.COMMAND_RESPONSE_KEY, command);
        data.put(CommandResponse.MESSAGE_KEY, message);
        response = new CommandResponse(data);
        assertThat(response.getCommand(), equalTo(command));
        assertThat(response.getMessage(), equalTo(message));
        assertFalse(response.contains(CommandResponse.COMMAND_RESPONSE_KEY));
        assertFalse(response.contains(CommandResponse.MESSAGE_KEY));
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
