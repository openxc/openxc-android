package com.openxc.measurements;

import com.openxc.units.Boolean;

/**
 * The BrakePedalStatus measurement knows if the brake pedal is pressed.
 */
public class BrakePedalStatus extends Measurement<Boolean> {
    public final static String ID = "brake_pedal_status";

    public BrakePedalStatus(Boolean value) {
        super(value);
    }

    public BrakePedalStatus(boolean value) {
        this(new Boolean(value));
    }

    public BrakePedalStatus(Double value) {
        this(new Boolean(value));
    }
}
