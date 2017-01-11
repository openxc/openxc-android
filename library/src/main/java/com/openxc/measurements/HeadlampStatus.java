package com.openxc.measurements;

/**
 * The HeadlampStatus measurement knows if the headlamps are off or on.
 */
public class HeadlampStatus extends BaseMeasurement<com.openxc.units.Boolean> {
    public final static String ID = "headlamp_status";

    public HeadlampStatus(com.openxc.units.Boolean value) {
        super(value);
    }

    public HeadlampStatus(java.lang.Boolean value) {
        this(new com.openxc.units.Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
