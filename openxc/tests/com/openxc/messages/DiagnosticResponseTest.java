package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
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
    boolean success = true;

    @Before
    public void setup() {
        response = new DiagnosticResponse(bus, id, mode, pid, payload, success);
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
    public void clearPayload() {
        response.setPayload(null);
        assertThat(response.getPayload(), nullValue());
    }

    @Test
    public void sameEquals() {
        assertEquals(response, response);
    }

    @Test
    public void differentIdNotEqual() {
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus, id + 1, mode, pid, payload, success);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void differentBusNotEqual() {
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus + 1, id, mode, pid, payload, success);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void differentModeNotEqual() {
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus, id, mode + 1, pid, payload, success);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void differentPayloadNotEqual() {
        payload[1] = (byte) (payload[1] + 1);
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus, id, mode, pid, payload, success);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void parcelWithNoPayload() {
        response.setPayload(null);
        Parcel parcel = Parcel.obtain();
        response.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        VehicleMessage createdFromParcel =
                VehicleMessage.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(DiagnosticResponse.class));
        assertEquals(response, createdFromParcel);
        assertThat(response.getPayload(), nullValue());
    }

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
