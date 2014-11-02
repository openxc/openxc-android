package com.openxc.measurements;

import java.util.Locale;

import com.openxc.units.State;

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
        extends EventedMeasurement<State<VehicleButtonEvent.ButtonId>> {
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
        OK
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
        STUCK
    }

    public VehicleButtonEvent(State<ButtonId> value,
            State<ButtonAction> event) {
        super(value, event);
    }

    public VehicleButtonEvent(ButtonId value, ButtonAction event) {
        this(new State<ButtonId>(value), new State<ButtonAction>(event));
    }

    public VehicleButtonEvent(String value, String event) {
        this(ButtonId.valueOf(value.toUpperCase(Locale.US)),
                ButtonAction.valueOf(event.toUpperCase(Locale.US)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public State<ButtonAction> getEvent() {
        return (State<ButtonAction>) super.getEvent();
    }

    @Override
    public String getSerializedEvent() {
        return getEvent().enumValue().toString();
    }

    @Override
    public String getSerializedValue() {
        return getValue().enumValue().toString();
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
