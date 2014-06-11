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
public class CommandMessageTest {
    CommandMessage message;
    String command = "foo";

    @Before
    public void setup() {
        message = new CommandMessage(command);
    }

    @Test
    public void getCommandReturnsCommand() {
        assertEquals(command, message.getCommand());
    }

    @Test
    public void extractsCommandFromValues()
            throws InvalidMessageFieldsException {
        HashMap<String, Object> data = new HashMap<>();
        data.put(CommandMessage.COMMAND_KEY, command);
        message = new CommandMessage(data);
        assertThat(message.getCommand(), equalTo(command));
        assertFalse(message.contains(CommandMessage.COMMAND_KEY));
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void sameCommandEquals() {
        CommandMessage anotherMessage = new CommandMessage(command);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(CommandMessage.class));
        assertEquals(message, createdFromParcel);
    }
}
