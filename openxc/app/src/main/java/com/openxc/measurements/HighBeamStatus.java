package com.openxc.measurements;

/**
 * The HighBeamStatus measurement knows if the high beams are on.
 */
public class HighBeamStatus extends BaseMeasurement<com.openxc.units.Boolean> {
    public final static String ID = "high_beam_status";

    public HighBeamStatus(com.openxc.units.Boolean value) {
        super(value);
    }

    public HighBeamStatus(java.lang.Boolean value) {
        this(new com.openxc.units.Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
