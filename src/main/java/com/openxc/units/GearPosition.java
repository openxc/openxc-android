package com.openxc.units;

public class GearPosition extends State<GearPosition.GearPositions> implements Unit {

    public enum GearPositions {
        FIRST,
        SECOND,
        THIRD,
        FOURTH,
        FIFTH,
        SIXTH,
        NEUTRAL,
        REVERSE
    }

    public GearPosition(GearPositions value) {
        super(value);
    }
}
