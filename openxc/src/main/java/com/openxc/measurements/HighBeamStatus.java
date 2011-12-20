package com.openxc.measurements;

import com.openxc.units.Boolean;

/**
 * The HighBeamStatus measurement knows if the high beams are on.
 */
public class HighBeamStatus extends Measurement<Boolean>
        implements VehicleMeasurement {
    public final static String ID = "high_beam_status";

    public HighBeamStatus() { }

    public HighBeamStatus(Boolean value) {
        super(value);
    }

    public HighBeamStatus(boolean value) {
        this(new Boolean(value));
    }

    public HighBeamStatus(Double value) {
        this(new Boolean(value));
    }
}
