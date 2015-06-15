package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.DiagnosticResponse;

import android.os.Parcel;

@RunWith(RobolectricTestRunner.class)
public class DiagnosticResponseTest {
    DiagnosticResponse response;
    DiagnosticResponse simpleResponse;
    int id = 42;
    int bus = 1;
    int mode = 2;
    int pid = 4;
    byte[] payload = new byte[] { 1, 2, 3, 4, 5, 6, 7 };

    @Before
    public void setup() {
        response = new DiagnosticResponse(bus, id, mode, pid, payload);
        simpleResponse = new DiagnosticResponse(bus, id, mode);
    }

    @Test
    public void simpleComplexResponseNotEqual() {
        assertThat(response, not(equalTo(simpleResponse)));
    }

    @Test
    public void requiredFieldsEqual() {
        assertEquals(response.getBusId(), simpleResponse.getBusId());
        assertEquals(response.getId(), simpleResponse.getId());
        assertEquals(response.getMode(), simpleResponse.getMode());
    }

    @Test
    public void nonEssentialFieldsNotEqual() {
        assertThat(response.getPid(), not(equalTo(simpleResponse.getPid())));
        assertThat(response.getPayload(), not(equalTo(simpleResponse.getPayload())));
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
    public void nullNegativeResponseMeansSuccess() {
        double value = 42.0;
        response = new DiagnosticResponse(bus, id, mode, pid, payload, null,
                value);
        assertTrue(response.isSuccessful());
        assertEquals(response.getNegativeResponseCode(),
                DiagnosticResponse.NegativeResponseCode.NONE);
    }

    @Test
    public void withNrcNotSuccessful() {
        response = new DiagnosticResponse(bus, id, mode, pid, payload,
                DiagnosticResponse.NegativeResponseCode.WRONG_BLOCK_SEQUENCE_COUNTER, 42);
        assertFalse(response.isSuccessful());
    }

    @Test
    public void getValueReturnsValue() {
        double value = 42.0;
        response = new DiagnosticResponse(bus, id, mode, pid, payload,
                DiagnosticResponse.NegativeResponseCode.NONE, value);
        assertTrue(response.hasValue());
        assertThat(response.getValue(), equalTo(value));
    }

    @Test
    public void getValueNoValueReturnsNull() {
        assertFalse(response.hasValue());
        assertThat(response.getValue(), nullValue());
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
                bus, id + 1, mode, pid, payload);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void differentBusNotEqual() {
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus + 1, id, mode, pid, payload);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void differentModeNotEqual() {
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus, id, mode + 1, pid, payload);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void differentPayloadNotEqual() {
        payload[1] = (byte) (payload[1] + 1);
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus, id, mode, pid, payload);
        anotherResponse.setPayload(payload);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void sameValueEqual() {
        double value = 42.0;
        response = new DiagnosticResponse(bus, id, mode, pid, payload, null,
                value);
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus, id, mode, pid, payload, null, value);
        assertThat(response, equalTo(anotherResponse));
    }

    @Test
    public void differentValueNotEqual() {
        double value = 42.0;
        response = new DiagnosticResponse(bus, id, mode, pid, payload, null,
                value);
        DiagnosticResponse anotherResponse = new DiagnosticResponse(
                bus, id, mode, pid, payload, null, value + 1);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void differentNrcNotEqual() {
        double value = 42.0;
        response = new DiagnosticResponse(bus, id, mode, pid, payload, null,
                value);
        DiagnosticResponse anotherResponse = new DiagnosticResponse(bus, id,
                mode, pid, payload,
                DiagnosticResponse.NegativeResponseCode.WRONG_BLOCK_SEQUENCE_COUNTER,
                value);
        assertThat(response, not(equalTo(anotherResponse)));
    }

    @Test
    public void toStringNotNull() {
        assertThat(response.toString(), notNullValue());
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

    @Test
    public void keyNotNull() {
        assertThat(response.getKey(), notNullValue());
    }
}
