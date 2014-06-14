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
public class DiagnosticRequestTest {
    DiagnosticRequest request;
    int id = 42;
    int bus = 1;
    int mode = 2;
    int pid = 4;
    byte[] payload = new byte[7];

    // TODO test building from values with missing keys

    @Before
    public void setup() {
        request = new DiagnosticRequest(bus, id, mode, pid, payload);
    }

    @Test
    public void getIdReturnsId() {
        assertEquals(id, request.getId());
    }

    @Test
    public void getBusReturnsBus() {
        assertEquals(bus, request.getBusId());
    }

    @Test
    public void getModeReturnsMode() {
        assertEquals(mode, request.getMode());
    }

    @Test
    public void getPayloadReturnsPayload() {
        assertArrayEquals(payload, request.getPayload());
    }

    @Test
    public void extractsFieldsFromValues()
            throws InvalidMessageFieldsException {
        HashMap<String, Object> values = new HashMap<>();
        values.put(DiagnosticRequest.ID_KEY, id);
        values.put(DiagnosticRequest.BUS_KEY, bus);
        values.put(DiagnosticRequest.MODE_KEY, mode);
        request = new DiagnosticRequest(values);

        assertThat(request.getId(), equalTo(id));
        assertThat(request.getBusId(), equalTo(bus));
        assertThat(request.getMode(), equalTo(mode));

        assertFalse(request.contains(DiagnosticRequest.ID_KEY));
        assertFalse(request.contains(DiagnosticRequest.BUS_KEY));
        assertFalse(request.contains(DiagnosticRequest.MODE_KEY));
    }

    // TODO test read/write payload and no payload, I know it's failing

    @Test
    public void sameEquals() {
        assertEquals(request, request);
    }

    // TODO check inequalities

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(DiagnosticRequest.class));
        assertEquals(request, createdFromParcel);
    }
}
