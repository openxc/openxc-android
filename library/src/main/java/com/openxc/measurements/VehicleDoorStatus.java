package com.openxc.measurements;

import java.util.Locale;

import com.openxc.units.State;

/**
 * A DoorStatus represents a door's ajar status.
 *
 * This measurement is only valid when used asynchronously, much like any other
 * key or button event in Java. An application registers to receive button
 * events, and decides what to do based on the returned ButtonId and
 * ButtonAction.
 *
 * TODO would you want to be able to query for a specific door's state
 * synchronously?
 */
public class VehicleDoorStatus
        extends EventedMeasurement<State<VehicleDoorStatus.DoorId>> {
    public final static String ID = "door_status";

    /**
     * The DoorId is the specific door of the vehicle.
     */
    public enum DoorId {
        DRIVER,
        PASSENGER,
        REAR_LEFT,
        REAR_RIGHT,
        BOOT
    }

    public VehicleDoorStatus(State<DoorId> value, com.openxc.units.Boolean event) {
        super(value, event);
    }

    public VehicleDoorStatus(DoorId value, com.openxc.units.Boolean event) {
        this(new State<>(value), event);
    }

    public VehicleDoorStatus(DoorId value, java.lang.Boolean event) {
        this(value, new com.openxc.units.Boolean(event));
    }

    public VehicleDoorStatus(String value, java.lang.Boolean event) {
        this(DoorId.valueOf(value.toUpperCase(Locale.US)), new com.openxc.units.Boolean(event));
    }

    @Override
    public com.openxc.units.Boolean getEvent() {
        return (com.openxc.units.Boolean) super.getEvent();
    }

    @Override
    public java.lang.Boolean getSerializedEvent() {
        return getEvent().booleanValue();
    }

    @Override
    public String getSerializedValue() {
        return getValue().enumValue().toString();
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
