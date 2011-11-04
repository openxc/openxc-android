package com.openxc.measurements;

import com.openxc.units.RotationsPerMinute;
import com.openxc.util.Range;

/* This may seem like a "psuedo-typedef" class but we're using it it to enforce
 * the binding between the measurement and its unit type. This unfortunately
 * means we have to add constructors because they aren't inherited from
 * Measurement. If you know of a better way, please speak up.
 */
public class EngineSpeed extends Measurement<RotationsPerMinute>
        implements VehicleMeasurement {
    private final static Range<RotationsPerMinute> RANGE =
        new Range<RotationsPerMinute>(new RotationsPerMinute(0),
                new RotationsPerMinute(8000));
    private final static String ID = "EngineSpeed";

    public EngineSpeed(RotationsPerMinute value) {
        super(value, RANGE);
    }

    public String getId() {
        return ID;
    }
}
