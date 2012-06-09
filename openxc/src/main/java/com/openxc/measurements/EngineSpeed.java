package com.openxc.measurements;

import com.openxc.units.RotationsPerMinute;
import com.openxc.util.Range;

/**
 * The EngineSpeed measurement represents the speed of the engine.
 *
 * The valid range for this measurement is from 0 to 8000 RotationsPerMinute.
 */
public class EngineSpeed extends BaseMeasurement<RotationsPerMinute> {
    private final static Range<RotationsPerMinute> RANGE =
        new Range<RotationsPerMinute>(new RotationsPerMinute(0),
                new RotationsPerMinute(16382));
    public final static String ID = "engine_speed";

    public EngineSpeed(Number value) {
        super(new RotationsPerMinute(value), RANGE);
    }
    public EngineSpeed(RotationsPerMinute value) {
        super(value, RANGE);
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
