package com.openxc.units;

public class Meter extends Quantity implements Unit {

    public Meter(double value) {
    }

    public boolean equalTo(double otherValue) {
        return false;
    }

    public double doubleValue() {
        return 0;
    }
}
