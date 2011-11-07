package com.openxc.measurements;

import com.openxc.units.RotationsPerMinute;
import com.openxc.util.Range;

public class EngineSpeed extends Measurement<RotationsPerMinute>
        implements VehicleMeasurement {
    private final static Range<RotationsPerMinute> RANGE =
        new Range<RotationsPerMinute>(new RotationsPerMinute(0),
                new RotationsPerMinute(8000));
    public final static String ID = "EngineSpeed";

    public EngineSpeed(RotationsPerMinute value) {
        super(value, RANGE);
    }
}
