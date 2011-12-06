package com.openxc.measurements;

import com.openxc.units.State;

import com.openxc.util.AgingData;

// This is by far the ugliest measurement. Is this implementation saying
// something about the architecture in general, or is it just an edge case we
// need to ignore and let live?
public class VehicleButtonEvent
        extends Measurement<State<VehicleButtonEvent.ButtonId>>
        implements VehicleMeasurement {
    private AgingData<State<ButtonAction>> mAction;

    public final static String ID = "button_event";

    public enum ButtonId {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        OK;

        private final int mHashCode;

        private ButtonId() {
            mHashCode = toString().hashCode();
        }

        public int getHashCode() {
            return mHashCode;
        }

        public static ButtonId fromHashCode(int hashCode) {
            for(ButtonId position : ButtonId.values()) {
                if(hashCode == position.getHashCode()) {
                    return position;
                }
            }
            throw new IllegalArgumentException(
                    "No valid button ID for hash code " + hashCode);
        }
    }

    public enum ButtonAction {
        IDLE,
        PRESSED,
        RELEASED,
        HELD_SHORT,
        HELD_LONG,
        STUCK;

        private final int mHashCode;

        private ButtonAction() {
            mHashCode = toString().hashCode();
        }

        public int getHashCode() {
            return mHashCode;
        }

        public static ButtonAction fromHashCode(int hashCode) {
            for(ButtonAction position : ButtonAction.values()) {
                if(hashCode == position.getHashCode()) {
                    return position;
                }
            }
            throw new IllegalArgumentException(
                    "No valid button action for hash code " + hashCode);
        }
    }

    public VehicleButtonEvent() {}

    public VehicleButtonEvent(State<ButtonId> value,
            State<ButtonAction> action) {
        super(value);
        mAction = new AgingData<State<ButtonAction>>(action);
    }

    public VehicleButtonEvent(ButtonId value, ButtonAction action) {
        this(new State<ButtonId>(value), new State<ButtonAction>(action));
    }

    public VehicleButtonEvent(Double value, Double action) {
        this(ButtonId.fromHashCode(value.intValue()),
                ButtonAction.fromHashCode(action.intValue()));
    }

    public State<ButtonAction> getAction() throws NoValueException {
        return mAction.getValue();
    }

    public boolean isNone() {
        return super.isNone() && mAction.isNone();
    }
}
