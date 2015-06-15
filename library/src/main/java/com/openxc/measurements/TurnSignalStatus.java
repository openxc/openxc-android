package com.openxc.measurements;

import java.util.Locale;

import com.openxc.units.State;

public class TurnSignalStatus extends
        BaseMeasurement<State<TurnSignalStatus.TurnSignalPosition>> {
    public final static String ID = "turn_signal_status";

    public enum TurnSignalPosition {
        OFF,
        LEFT,
        RIGHT
    }

    public TurnSignalStatus(State<TurnSignalPosition> value) {
        super(value);
    }

    public TurnSignalStatus(TurnSignalPosition value) {
        this(new State<>(value));
    }

    public TurnSignalStatus(String value) {
        this(TurnSignalPosition.valueOf(value.toUpperCase(Locale.US)));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
