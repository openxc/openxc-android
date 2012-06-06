package com.openxc.measurements;

import com.openxc.units.State;

public class TurnSignalStatus extends
        Measurement<State<TurnSignalStatus.TurnSignalPosition>> {
    public final static String ID = "turn_signal_status";

    public enum TurnSignalPosition {
        OFF,
        LEFT,
        RIGHT;

        private final int mHashCode;

        private TurnSignalPosition() {
            mHashCode = toString().hashCode();
        }

        public int getHashCode() {
            return mHashCode;
        }

        public static TurnSignalPosition fromHashCode(int hashCode) {
            for(TurnSignalPosition position : TurnSignalPosition.values()) {
                if(hashCode == position.getHashCode()) {
                    return position;
                }
            }
            throw new IllegalArgumentException(
                    "No valid turn signal position for hash code " + hashCode);
        }
    }

    public TurnSignalStatus(State<TurnSignalPosition> value) {
        super(value);
    }

    public TurnSignalStatus(TurnSignalPosition value) {
        this(new State<TurnSignalPosition>(value));
    }

    public TurnSignalStatus(Double value) {
        this(TurnSignalPosition.fromHashCode(value.intValue()));
    }
}
