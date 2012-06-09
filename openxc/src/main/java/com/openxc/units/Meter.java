package com.openxc.units;

/**
 * A Meter is the base unit of length in the SI.
 */
public class Meter extends Quantity<Number> implements Unit {

    public Meter(Number value) {
        super(value);
    }
}
