package com.openxc.measurements;

import com.openxc.units.Boolean;

/**
 * The HeadlampStatus measurement knows if the headlamps are off or on.
 */
public class HeadlampStatus extends BaseMeasurement<Boolean> {
    public final static String ID = "headlamp_status";

    public HeadlampStatus(Boolean value) {
        super(value);
    }

    public HeadlampStatus(java.lang.Boolean value) {
        this(new Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
