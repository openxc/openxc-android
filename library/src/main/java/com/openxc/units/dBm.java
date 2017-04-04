package com.openxc.units;

/**
 * dBm is a measure of TBD.
 *
 * This is commonly used with regard to modem signal strength.
 */
public class dBm extends Quantity<Number> {
    private final String TYPE_STRING = "dBm";

    public dBm(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
