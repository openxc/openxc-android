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

import com.openxc.messages.Command.CommandType;

@RunWith(RobolectricTestRunner.class)
public class CommandTest {
    Command message;
    CommandType command = CommandType.VERSION;
    String action = DiagnosticRequest.ADD_ACTION_KEY;
    DiagnosticRequest request = new DiagnosticRequest(1, 2, 3, 4);

    @Before
    public void setup() {
        message = new Command(command);
    }

    @Test
    public void getCommandReturnsCommand() {
        assertEquals(command, message.getCommand());
    }

    @Test
    public void sameEquals() {
        assertEquals(message, message);
    }

    @Test
    public void sameCommandEquals() {
        Command anotherMessage = new Command(command);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void sameDiagnosticRequestEqual() {
        message = new Command(request, action);
        Command anotherMessage = new Command(new DiagnosticRequest(1, 2, 3, 4), action);
        assertEquals(message, anotherMessage);
    }

    @Test
    public void differentDiagnosticRequestNotEqual() {
        message = new Command(request, action);
        Command anotherMessage = new Command(new DiagnosticRequest(5, 6, 7, 8), action);
        assertThat(message, not(equalTo(anotherMessage)));
    }

    @Test
    public void differentActionNotEqual() {
        message = new Command(command, action);
        Command anotherMessage = new Command(command, "another");
        assertThat(message, not(equalTo(anotherMessage)));
    }

    @Test
    public void differentDiagnosticRequestActionNotEqual() {
        message = new Command(request, action);
        Command anotherMessage = new Command(new DiagnosticRequest(5, 6, 7, 8),
                "another");
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
        assertThat(createdFromParcel, instanceOf(Command.class));
        assertEquals(message, createdFromParcel);
    }

    @Test
    public void writeAndReadFromParcelWithDiagnostic() {
        message = new Command(request, action);
        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(Command.class));
        assertEquals(message, createdFromParcel);
    }

    @Test
    public void keyNotNull() {
        assertThat(message.getKey(), notNullValue());
    }
}
