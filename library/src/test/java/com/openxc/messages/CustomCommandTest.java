package com.openxc.messages;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class CustomCommandTest {
    CustomCommand customCommand;
    String inputString = "{ \"command\": \"version\"}";
    HashMap<String,String> inputCommand = getMapFromJson(inputString);


    @Before
    public void setup() {
        customCommand = new CustomCommand(inputCommand);
    }

    @Test
    public void getCommandReturnsCommand() {
        assertEquals(inputCommand, customCommand.getCommands());
    }

    @Test
    public void sameEquals() {
        assertEquals(customCommand, customCommand);
    }

    @Test
    public void sameCommandEquals() {
        CustomCommand anotherMessage = new CustomCommand(inputCommand);
        assertEquals(customCommand, anotherMessage);
    }

    @Test
    public void toStringNotNull() {
        assertThat(customCommand.toString(), notNullValue());
    }

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        customCommand.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(CustomCommand.class));
        assertEquals(customCommand, createdFromParcel);
    }

    @Test
    public void writeAndReadFromParcelWithDiagnostic() {
        customCommand = new CustomCommand(inputCommand);
        Parcel parcel = Parcel.obtain();
        customCommand.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(CustomCommand.class));
        assertEquals(customCommand, createdFromParcel);
    }

    @Test
    public void keyNotNull() {
        assertThat(customCommand.getKey(), notNullValue());
    }

    /****
     * This method will return a map of the key value pairs received from JSON.
     * If JSON is invalid it will return null.
     * @param customJson
     * @return HashMap
     */
    private HashMap<String, String> getMapFromJson(String customJson) {
        HashMap<String, String> command = new HashMap<>();
        if (customJson != null) {
            try {
                JSONObject jsonObject = new JSONObject(customJson);
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = jsonObject.getString(key);
                    command.put(key, value);
                }
                return command;
            } catch (JSONException exception) {
                return null;
            }
        } else
            return null;
    }
}
