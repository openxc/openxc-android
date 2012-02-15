package com.openxc.measurements;

import com.openxc.units.Boolean;

public class WindshieldWiperStatus extends Measurement<Boolean>
        implements VehicleMeasurement {
    public final static String ID = "windshield_wiper_status";

    public WindshieldWiperStatus(Boolean value) {
        super(value);
    }

    public WindshieldWiperStatus(Double value) {
        super(new Boolean(value));
    }

    public WindshieldWiperStatus(boolean value) {
        this(new Boolean(value));
    }
}
