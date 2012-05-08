package com.openxc.measurements;

import com.openxc.units.Boolean;

/**
 * The HeadlampStatus measurement knows if the headlamps are off or on.
 */
public class HeadlampStatus extends Measurement<Boolean> {
    public final static String ID = "headlamp_status";

    public HeadlampStatus(Boolean value) {
        super(value);
    }

    public HeadlampStatus(boolean value) {
        this(new Boolean(value));
    }

    public HeadlampStatus(Double value) {
        this(new Boolean(value));
    }
}
