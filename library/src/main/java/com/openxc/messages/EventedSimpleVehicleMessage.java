package com.openxc.messages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

/**
 * A simple vehicle message also with an event field.
 *
 * An EventedSimpleVehicleMessage can optionally be converted into a
 * Measurement, e.g. a VehicleDoorStatus measurement.
 */
public class EventedSimpleVehicleMessage extends SimpleVehicleMessage {
    private static final String EVENT_KEY = "event";

    private static final String[] sRequiredFieldsValues = new String[] {
            NAME_KEY, VALUE_KEY, EVENT_KEY };
    private static final Set<String> sRequiredFields = new HashSet<>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(EVENT_KEY)
    private Object mEvent;

    public EventedSimpleVehicleMessage(Long timestamp, String name, Object value,
            Object event) {
        super(timestamp, name, value);
        mEvent = event;
    }

    public EventedSimpleVehicleMessage(String name, Object value, Object event) {
        this(null, name, value, event);
    }

    public Object getEvent() {
        return mEvent;
    }

    public Number getEventAsNumber() {
        return (Number) mEvent;
    }

    public String getEventAsString() {
        return (String) mEvent;
    }

    public Boolean getEventAsBoolean() {
        return (Boolean) mEvent;
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || !(obj instanceof EventedSimpleVehicleMessage)) {
            return false;
        }

        final EventedSimpleVehicleMessage other = (EventedSimpleVehicleMessage) obj;
        return mEvent.equals(other.mEvent);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("name", getName())
            .add("value", getValue())
            .add("event", getEvent())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeValue(getEvent());
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mEvent = in.readValue(null);
    }

    protected EventedSimpleVehicleMessage(Parcel in) {
        readFromParcel(in);
    }

    protected EventedSimpleVehicleMessage() { }
}
