package com.openxc.measurements;

import com.openxc.units.Boolean;

/**
 * The HighBeamStatus measurement knows if the high beams are on.
 */
public class HighBeamStatus extends BaseMeasurement<Boolean> {
    public final static String ID = "high_beam_status";

    public HighBeamStatus(Boolean value) {
        super(value);
    }

    public HighBeamStatus(java.lang.Boolean value) {
        this(new Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
