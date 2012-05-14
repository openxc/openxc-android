package com.openxc.measurements;

import com.openxc.units.State;

import com.openxc.util.AgingData;

/**
 * A ButtonEvent represents a button press, release or hold on the vehicle HMI.
 *
 * This measurement is only valid when used asynchronously, much like any other
 * key or button event in Java. An application registers to receive button
 * events, and decides what to do based on the returned ButtonId and
 * ButtonAction.
 *
 * If this measurement is retrieved synchronously, it will return the last
 * button event received - this is probably not what you want.
 *
 * TODO This is by far the ugliest measurement because it has to incorporate two
 * different values instead of the usual one. Is this implementation saying
 * something about the architecture in general, or is it just an edge case we
 * need to ignore and let live?
 */
public class VehicleButtonEvent
        extends Measurement<State<VehicleButtonEvent.ButtonId>> {
    public final static String ID = "button_event";

    /**
     * The ButtonId is the direction of a button within a single control
     * cluster.
     */
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

    /**
     * The ButtonAction is the specific event that ocurred.
     */
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

    public VehicleButtonEvent(State<ButtonId> value,
            State<ButtonAction> event) {
        super(value, event);
    }

    public VehicleButtonEvent(ButtonId value, ButtonAction event) {
        this(new State<ButtonId>(value), new State<ButtonAction>(event));
    }

    public VehicleButtonEvent(Double value, Double event) {
        this(ButtonId.fromHashCode(value.intValue()),
                ButtonAction.fromHashCode(event.intValue()));
    }

    @Override
    public State<ButtonAction> getEvent() {
        return (State<ButtonAction>) super.getEvent();
    }
}
