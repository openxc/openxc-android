package com.openxc.units;

/**
 * Percentage, a unit expressing a number as a fraction of 100.
 */
public class Percentage extends Quantity<Number> implements Unit {

    public Percentage(Number value) {
        super(value);
    }
}
