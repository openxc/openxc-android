package com.openxc.measurements;

import com.google.common.base.Objects;

import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.units.Unit;
import com.openxc.util.AgingData;

public abstract class EventedMeasurement<TheUnit extends Unit>
        extends BaseMeasurement<TheUnit> {

    private AgingData<Unit> mEvent;

    public abstract Object getSerializedEvent();

    public EventedMeasurement(TheUnit value, Unit event) {
        super(value);
        mEvent = new AgingData<>(event);
    }

    @Override
    public SimpleVehicleMessage toVehicleMessage() {
        return new EventedSimpleVehicleMessage(
                mValue.getTimestamp(), getGenericName(),
                getSerializedValue(), getSerializedEvent());
    }

    public Object getEvent() {
        if(hasEvent()) {
            return mEvent.getValue();
        }
        return null;
    }

    public boolean hasEvent() {
        return mEvent != null;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final EventedMeasurement<TheUnit> other = (EventedMeasurement<TheUnit>) obj;
        return Objects.equal(getEvent(), other.getEvent());
    }
}
