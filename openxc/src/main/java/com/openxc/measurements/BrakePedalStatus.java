package com.openxc.measurements;

import com.openxc.units.Boolean;

/**
 * The BrakePedalStatus measurement knows if the brake pedal is pressed.
 */
public class BrakePedalStatus extends BaseMeasurement<Boolean> {
    public final static String ID = "brake_pedal_status";

    public BrakePedalStatus(Boolean value) {
        super(value);
    }

    public BrakePedalStatus(java.lang.Boolean value) {
        this(new Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
