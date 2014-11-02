package com.openxc.measurements;

import java.util.Locale;

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
        START
    }

    public IgnitionStatus(State<IgnitionPosition> value) {
        super(value);
    }

    public IgnitionStatus(IgnitionPosition value) {
        this(new State<IgnitionPosition>(value));
    }

    public IgnitionStatus(String value) {
        this(IgnitionPosition.valueOf(value.toUpperCase(Locale.US)));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
