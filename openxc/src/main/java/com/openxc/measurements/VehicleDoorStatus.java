package com.openxc.measurements;

import com.openxc.units.State;

import com.openxc.util.AgingData;
import com.openxc.units.Boolean;

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
        extends Measurement<State<VehicleDoorStatus.DoorId>>
        implements VehicleMeasurement {
    private AgingData<Boolean> mAction;

    public final static String ID = "door_status";

    /**
     * The DoorId is the specific door of the vehicle.
     */
    public enum DoorId {
        DRIVER,
        PASSENGER,
        REAR_LEFT,
        REAR_RIGHT,
        BOOT;

        private final int mHashCode;

        private DoorId() {
            mHashCode = toString().hashCode();
        }

        public int getHashCode() {
            return mHashCode;
        }

        public static DoorId fromHashCode(int hashCode) {
            for(DoorId position : DoorId.values()) {
                if(hashCode == position.getHashCode()) {
                    return position;
                }
            }
            throw new IllegalArgumentException(
                    "No valid door ID for hash code " + hashCode);
        }
    }

    public VehicleDoorStatus(State<DoorId> value, Boolean action) {
        super(value);
        mAction = new AgingData<Boolean>(action);
    }

    public VehicleDoorStatus(DoorId value, Boolean action) {
        this(new State<DoorId>(value), action);
    }

    public VehicleDoorStatus(Double value, Double action) {
        this(DoorId.fromHashCode(value.intValue()), new Boolean(action));
    }

    public Boolean getAction() {
        return mAction.getValue();
    }
}
