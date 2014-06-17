package com.openxc.messages;

import java.util.HashMap;

import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import android.os.Parcel;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DiagnosticRequestTest {
    DiagnosticRequest request;
    int id = 42;
    int bus = 1;
    int mode = 2;
    int pid = 4;
    byte[] payload = new byte[]{1, 2, 3, 4, 5, 6, 7};
    HashMap<String, Object> values;

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
        request = new DiagnosticRequest(bus, id, mode, pid, payload);

        values = new HashMap<>();
        values.put(DiagnosticRequest.BUS_KEY, bus);
        values.put(DiagnosticRequest.ID_KEY, id);
        values.put(DiagnosticRequest.MODE_KEY, mode);
    }

    @Test
    public void checkRequiredFields() {
        assertTrue(DiagnosticRequest.containsAllRequiredFields(values));
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
    public void getPidWithNoPidReturnsNull()
            throws InvalidMessageFieldsException {
        DiagnosticRequest noPidRequest = new DiagnosticRequest(values);
        assertThat(noPidRequest.getPid(), nullValue());
    }

    @Test
    public void getNoNameReturnsNull() {
        assertThat(request.getName(), nullValue());
    }

    @Test
    public void getPayloadReturnsPayload() {
        assertArrayEquals(payload, request.getPayload());
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildEmptyValues() throws InvalidMessageFieldsException {
        values = new HashMap<>();
        new DiagnosticRequest(values);
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildMissingId() throws InvalidMessageFieldsException {
        values.remove(DiagnosticRequest.ID_KEY);
        new DiagnosticRequest(values);
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildMissingBus() throws InvalidMessageFieldsException {
        values.remove(DiagnosticRequest.BUS_KEY);
        new DiagnosticRequest(values);
    }

    @Test(expected=InvalidMessageFieldsException.class)
    public void buildMissingMode() throws InvalidMessageFieldsException {
        values.remove(DiagnosticRequest.MODE_KEY);
        new DiagnosticRequest(values);
    }

    @Test
    public void includeOptionalPid() throws InvalidMessageFieldsException {
        values.put(DiagnosticRequest.PID_KEY, pid);
        request = new DiagnosticRequest(values);
        assertThat(request.getPid(), equalTo(pid));
    }

    @Test
    public void includeOptionalPayload() throws InvalidMessageFieldsException {
        values.put(DiagnosticRequest.PAYLOAD_KEY, payload);
        request = new DiagnosticRequest(values);
        assertThat(request.getPayload(), equalTo(payload));
    }

    @Test
    public void extractsFieldsFromValues()
            throws InvalidMessageFieldsException {
        request = new DiagnosticRequest(values);

        assertThat(request.getId(), equalTo(id));
        assertThat(request.getBusId(), equalTo(bus));
        assertThat(request.getMode(), equalTo(mode));

        assertFalse(request.contains(DiagnosticRequest.ID_KEY));
        assertFalse(request.contains(DiagnosticRequest.BUS_KEY));
        assertFalse(request.contains(DiagnosticRequest.MODE_KEY));
    }

    @Test
    public void sameEquals() {
        assertEquals(request, request);
    }

    @Test
    public void differentIdNotEqual() {
        DiagnosticRequest anotherRequest = new DiagnosticRequest(
                id + 1, bus, mode, payload);
        assertThat(request, not(equalTo(anotherRequest)));
    }

    @Test
    public void differentBusNotEqual() {
        DiagnosticRequest anotherRequest = new DiagnosticRequest(
                id, bus + 1, mode, payload);
        assertThat(request, not(equalTo(anotherRequest)));
    }

    @Test
    public void differentModeNotEqual() {
        DiagnosticRequest anotherRequest = new DiagnosticRequest(
                id, bus, mode + 1, payload);
        assertThat(request, not(equalTo(anotherRequest)));
    }

    @Test
    public void differentPayloadNotEqual() {
        payload[1] = (byte) (payload[1] + 1);
        DiagnosticRequest anotherRequest = new DiagnosticRequest(
                id, bus, mode, payload);
        assertThat(request, not(equalTo(anotherRequest)));
    }

    @Test
    public void writeAndReadFromParcelWithPayloadAndPid() {
        Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(DiagnosticRequest.class));
        assertEquals(request, createdFromParcel);
    }

    @Test
    public void writeAndReadFromParcelNoOptionalFields()
            throws InvalidMessageFieldsException {
        request = new DiagnosticRequest(values);

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
