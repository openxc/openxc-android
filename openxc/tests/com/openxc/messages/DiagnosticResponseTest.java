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
public class DiagnosticResponseTest {
    DiagnosticResponse response;
    int id = 42;
    int bus = 1;
    int mode = 2;
    int pid = 4;
    byte[] payload = new byte[] { 1, 2, 3, 4, 5, 6, 7 };

    // TODO test building from values with missing keys

    @Before
    public void setup() {
        response = new DiagnosticResponse(
                new DiagnosticRequest(bus, id, mode, pid), payload, true);
    }

    @Test
    public void getIdReturnsId() {
        assertEquals(id, response.getId());
    }

    @Test
    public void getBusReturnsBus() {
        assertEquals(bus, response.getBusId());
    }

    @Test
    public void getModeReturnsMode() {
        assertEquals(mode, response.getMode());
    }

    @Test
    public void getPayloadReturnsPayload() {
        assertArrayEquals(payload, response.getPayload());
    }

    @Test
    public void extractsFieldsFromValues()
            throws InvalidMessageFieldsException {
        HashMap<String, Object> values = new HashMap<>();
        values.put(DiagnosticRequest.ID_KEY, id);
        values.put(DiagnosticRequest.BUS_KEY, bus);
        values.put(DiagnosticRequest.MODE_KEY, mode);
        values.put(DiagnosticResponse.SUCCESS_KEY, true);
        response = new DiagnosticResponse(values);

        assertThat(response.getId(), equalTo(id));
        assertThat(response.getBusId(), equalTo(bus));
        assertThat(response.getMode(), equalTo(mode));

        assertFalse(response.contains(DiagnosticRequest.ID_KEY));
        assertFalse(response.contains(DiagnosticRequest.BUS_KEY));
        assertFalse(response.contains(DiagnosticRequest.MODE_KEY));
    }

    // TODO test read/write payload and no payload, I know it's failing

    @Test
    public void sameEquals() {
        assertEquals(response, response);
    }

    // TODO check inequalities

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        response.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(DiagnosticResponse.class));
        assertEquals(response, createdFromParcel);
    }
}
