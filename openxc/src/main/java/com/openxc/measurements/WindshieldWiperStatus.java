package com.openxc.measurements;

import com.openxc.units.Boolean;

public class WindshieldWiperStatus extends BaseMeasurement<Boolean> {
    private final static String ID = "windshield_wiper_status";

    public WindshieldWiperStatus(Boolean value) {
        super(value);
    }

    public WindshieldWiperStatus(java.lang.Boolean value) {
        this(new Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
