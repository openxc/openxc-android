package com.openxc.measurements;

import com.openxc.units.State;

public class TransmissionGearPosition
        extends Measurement<State<TransmissionGearPosition.GearPosition>>
        implements VehicleMeasurement {
    public final static String ID = "transmission_gear_position";

    public enum GearPosition {
        // TODO this could also be done using the ordinal values of the enum,
        // but that is less explicit and requires that these be in the same
        // order as defined in the specification. it would allow us to delete
        // all of this code though...
        FIRST(1),
        SECOND(2),
        THIRD(3),
        FOURTH(4),
        FIFTH(5),
        SIXTH(6),
        SEVENTH(7),
        EIGHTH(8),
        NEUTRAL(9),
        REVERSE(10);

        private final int mNumericalValue;

        private GearPosition(int value) {
            mNumericalValue = value;
        }

        public int getInt() {
            return mNumericalValue;
        }

        public static GearPosition fromInt(int value) {
            for(GearPosition position : GearPosition.values()) {
                if(value == position.getInt()) {
                    return position;
                }
            }
            throw new IllegalArgumentException(
                    "No valid gear position for int " + value);
        }
    }

    public TransmissionGearPosition() {}

    public TransmissionGearPosition(State<GearPosition> value) {
        super(value);
    }

    public TransmissionGearPosition(GearPosition value) {
        this(new State<GearPosition>(value));
    }

    public TransmissionGearPosition(Double value) {
        this(GearPosition.fromInt(value.intValue()));
    }
}
