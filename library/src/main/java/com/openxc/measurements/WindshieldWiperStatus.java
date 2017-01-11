package com.openxc.measurements;

public class WindshieldWiperStatus extends BaseMeasurement<com.openxc.units.Boolean> {
    public final static String ID = "windshield_wiper_status";

    public WindshieldWiperStatus(com.openxc.units.Boolean value) {
        super(value);
    }

    public WindshieldWiperStatus(java.lang.Boolean value) {
        this(new com.openxc.units.Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
