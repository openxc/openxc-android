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

    // Superset composed of the sub signals within HS1: 0x230 for both
    // GearPos_D_Trg : Neutral, First, Second, Third, Fourth, Fifth, Sixth, Seventh,
    //                      Eighth, Ninth, Tenth, Reverse
    //    and
    // GearLvrPos_D_Actl : Park, Reverse, Neutral, Drive, Sport_DriveSport, Low, first
    //                      second, third, fourth, fifth, sizth
    //

    public enum GearPosition {
        FIRST,
        SECOND,
        THIRD,
        FOURTH,
        FIFTH,
        SIXTH,
        SEVENTH,
        EIGHTH,
        NINTH,
        TENTH,
        NEUTRAL,
        REVERSE,
        PARK,
        DRIVE,
        SPORT_DRIVESPORT,
        LOW
    }

    public TransmissionGearPosition(State<GearPosition> value) {
        super(value);
    }

    public TransmissionGearPosition(GearPosition value) {
        this(new State<>(value));
    }

    public TransmissionGearPosition(String value) {
        this(GearPosition.valueOf(value.toUpperCase(Locale.US)));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
