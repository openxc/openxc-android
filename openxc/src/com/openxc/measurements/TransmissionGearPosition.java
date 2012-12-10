package com.openxc.measurements;

import java.util.Locale;

import com.openxc.units.State;

/**
 * The TransmissionGearPosition is the actual current gear of the transmission.
 *
 * This measurement is the current actual gear, not the selected or desired gear
 * by the driver or computer.
 */
public class TransmissionGearPosition
        extends BaseMeasurement<State<TransmissionGearPosition.GearPosition>> {
    public final static String ID = "transmission_gear_position";

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
        REVERSE
    }

    public TransmissionGearPosition(State<GearPosition> value) {
        super(value);
    }

    public TransmissionGearPosition(GearPosition value) {
        this(new State<GearPosition>(value));
    }

    public TransmissionGearPosition(String value) {
        this(GearPosition.valueOf(value.toUpperCase(Locale.US)));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
