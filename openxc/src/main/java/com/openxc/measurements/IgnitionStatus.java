package com.openxc.measurements;

import com.openxc.units.State;

/**
 * The IgnitionStatus is the current status of the vehicle's ignition.
 */
public class IgnitionStatus
        extends BaseMeasurement<State<IgnitionStatus.IgnitionPosition>> {
    public final static String ID = "ignition_status";

    public enum IgnitionPosition {
        OFF,
        ACCESSORY,
        RUN,
        START;

        private final int mHashCode;

        private IgnitionPosition() {
            mHashCode = toString().hashCode();
        }

        public int getHashCode() {
            return mHashCode;
        }

        public static IgnitionPosition fromHashCode(int hashCode) {
            for(IgnitionPosition position : IgnitionPosition.values()) {
                if(hashCode == position.getHashCode()) {
                    return position;
                }
            }
            throw new IllegalArgumentException(
                    "No valid ignition position for hash code " + hashCode);
        }
    }

    public IgnitionStatus(State<IgnitionPosition> value) {
        super(value);
    }

    public IgnitionStatus(IgnitionPosition value) {
        this(new State<IgnitionPosition>(value));
    }

    public IgnitionStatus(Double value) {
        this(IgnitionPosition.fromHashCode(value.intValue()));
    }
}
