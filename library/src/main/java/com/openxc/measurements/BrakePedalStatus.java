package com.openxc.measurements;

/**
 * The BrakePedalStatus measurement knows if the brake pedal is pressed.
 */
public class BrakePedalStatus extends BaseMeasurement<com.openxc.units.Boolean> {
    public final static String ID = "brake_pedal_status";

    public BrakePedalStatus(com.openxc.units.Boolean value) {
        super(value);
    }

    public BrakePedalStatus(Boolean value) {
        this(new com.openxc.units.Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
