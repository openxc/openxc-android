package com.openxc.measurements;

import com.openxc.units.State;

/**
 * The TransmissionGearPosition is the actual current gear of the transmission.
 *
 * This measurement is the current actual gear, not the selected or desired gear
 * by the driver or computer.
 */
public class TransmissionGearPosition
        extends BaseMeasurement<State<TransmissionGearPosition.GearPosition>> {
    private final static String ID = "transmission_gear_position";

    public enum GearPosition {
        FIRST,
        SECOND,
        THIRD,
        FOURTH,
        FIFTH,
        SIXTH,
        SEVENTH,
        EIGHTH,
        NEUTRAL,
        REVERSE;

        private final int mHashCode;

        private GearPosition() {
            mHashCode = toString().hashCode();
        }

        public int getHashCode() {
            return mHashCode;
        }

        public static GearPosition fromHashCode(int hashCode) {
            for(GearPosition position : GearPosition.values()) {
                if(hashCode == position.getHashCode()) {
                    return position;
                }
            }
            throw new IllegalArgumentException(
                    "No valid gear position for hash code " + hashCode);
        }
    }

    public TransmissionGearPosition(State<GearPosition> value) {
        super(value);
    }

    public TransmissionGearPosition(GearPosition value) {
        this(new State<GearPosition>(value));
    }

    public TransmissionGearPosition(Double value) {
        this(GearPosition.fromHashCode(value.intValue()));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
