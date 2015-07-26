package com.openxc.measurements;

import com.google.common.base.Objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.openxc.NoValueException;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.MessageKey;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.units.Unit;
import com.openxc.util.AgingData;
import com.openxc.util.Range;

/**
 * The BaseMeasurement is the base implementation of the Measurement, and
 * wraps an instance of a {@link Unit}, and the value it returns is always
 * in terms of this Unit.
 *
 * The Unit wrapper might seem annoying at first, but it is critical to avoid
 * misinterpreting the unit and crashing your lander into Mars
 * (http://en.wikipedia.org/wiki/Mars_Climate_Orbiter).
 *
 * Most applications will not use this class directly, but will import specific
 * child classes that correspond to specific types of measurements - i.e. the
 * parameterized instances of this class with a Unit. That may seem like a
 * "pseudo-typedef" but we're using it to enforce the binding between
 * the measurement and its unit type. This unfortunately means we have to add
 * constructors to every child class because they aren't inherited from
 * Measurement. If you know of a better way, please say so.
 */
public abstract class BaseMeasurement<TheUnit extends Unit>
            implements Measurement {
    private static final String TAG = BaseMeasurement.class.toString();

    protected AgingData<TheUnit> mValue;
    private Range<TheUnit> mRange;
    private static Map<Class<? extends Measurement>, String>
            sCachedPrettyNames = new HashMap<>();
    private static BiMap<String, Class<? extends Measurement>>
            sMeasurementIdToClass;

    static {
        sMeasurementIdToClass = HashBiMap.create();
        try {
            cacheMeasurementId(AcceleratorPedalPosition.class);
            cacheMeasurementId(BrakePedalStatus.class);
            cacheMeasurementId(EngineSpeed.class);
            cacheMeasurementId(FuelConsumed.class);
            cacheMeasurementId(FuelLevel.class);
            cacheMeasurementId(HeadlampStatus.class);
            cacheMeasurementId(HighBeamStatus.class);
            cacheMeasurementId(IgnitionStatus.class);
            cacheMeasurementId(Latitude.class);
            cacheMeasurementId(Longitude.class);
            cacheMeasurementId(Odometer.class);
            cacheMeasurementId(ParkingBrakeStatus.class);
            cacheMeasurementId(SteeringWheelAngle.class);
            cacheMeasurementId(TorqueAtTransmission.class);
            cacheMeasurementId(TransmissionGearPosition.class);
            cacheMeasurementId(TurnSignalStatus.class);
            cacheMeasurementId(VehicleButtonEvent.class);
            cacheMeasurementId(VehicleDoorStatus.class);
            cacheMeasurementId(VehicleSpeed.class);
            cacheMeasurementId(WindshieldWiperStatus.class);
        } catch(UnrecognizedMeasurementTypeException e) { }
    }

    public abstract String getGenericName();

    /**
     * Construct a new Measurement with the given value.
     *
     * @param value the TheUnit this measurement represents.
     */
    public BaseMeasurement(TheUnit value) {
        if(!sMeasurementIdToClass.inverse().containsKey(this.getClass())) {
            try {
                cacheMeasurementId(this.getClass());
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "Incomplete BaseMeasurement subclass", e);
            }
        }
        mValue = new AgingData<>(value);
    }

    /**
     * Construct an new Measurement with the given value and valid Range.
     *
     * There is not currently any automated verification that the value is
     * within the range - this is up to the application programmer.
     *
     * @param value the TheUnit this measurement represents.
     * @param range the valid {@link Range} of values for this measurement.
     */
    public BaseMeasurement(TheUnit value, Range<TheUnit> range) {
        this(value);
        mRange = range;
    }

    @Override
    public void setTimestamp(long timestamp) {
        mValue.setTimestamp(timestamp);
    }

    @Override
    public long getAge() {
        return mValue.getAge();
    }

    @Override
    public long getBirthtime() {
        return mValue.getTimestamp();
    }

    @Override
    public boolean hasRange() {
        return mRange != null;
    }

    @Override
    public Range<TheUnit> getRange() {
        return mRange;
    }

    @Override
    public TheUnit getValue() {
        return mValue.getValue();
    }

    @Override
    public Object getSerializedValue() {
        return getValue().getSerializedValue();
    }

    @Override
    public SimpleVehicleMessage toVehicleMessage() {
        return new SimpleVehicleMessage(mValue.getTimestamp(),
                getGenericName(), getSerializedValue());
    }

    public String getName(Context context) {
        String name = getGenericName();
        if(!sCachedPrettyNames.containsKey(getClass())) {
            // Make sure to not use the package name here, we have to find the
            // resource using the package name of the app using the library instead.
            int identifier = context.getResources().getIdentifier(
                    getGenericName() + "_label", "string", context.getPackageName());
            if(identifier != 0) {
                name = context.getString(identifier);
                sCachedPrettyNames.put(getClass(), name);
            }
        } else if(sCachedPrettyNames.get(getClass()) != null) {
            name = sCachedPrettyNames.get(getClass());
        }
        return name;
    }

    private static void cacheMeasurementId(
            Class<? extends Measurement> measurementType)
            throws UnrecognizedMeasurementTypeException {
        String measurementId;
        try {
            measurementId = (String) measurementType.getField("ID").get(
                    measurementType);
            sMeasurementIdToClass.put(measurementId, measurementType);
        } catch(NoSuchFieldException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " doesn't have an ID field", e);
        } catch(IllegalAccessException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " has an inaccessible " +
                    "ID field", e);
        }
    }

    public static MessageKey getKeyForMeasurement(
            Class<? extends Measurement> measurementType)
            throws UnrecognizedMeasurementTypeException {
        if(!sMeasurementIdToClass.inverse().containsKey(measurementType)) {
            cacheMeasurementId(measurementType);
        }
        return new NamedVehicleMessage(
                sMeasurementIdToClass.inverse().get(measurementType)).getKey();
    }

    public static Class<? extends Measurement>
            getClassForId(String measurementId)
            throws UnrecognizedMeasurementTypeException {
        Class<? extends Measurement> result = sMeasurementIdToClass.get(
                measurementId);
        if(result == null) {
            throw new UnrecognizedMeasurementTypeException(
                    "Didn't have a measurement with ID " + measurementId +
                    " cached");
        }
        return result;
    }

    public static Measurement getMeasurementFromMessage(
            SimpleVehicleMessage message)
                throws UnrecognizedMeasurementTypeException, NoValueException {
        Class<? extends Measurement> measurementClass =
            BaseMeasurement.getClassForId(message.getName());
        return BaseMeasurement.getMeasurementFromMessage(measurementClass,
                message);
    }

    public static Measurement getMeasurementFromMessage(
            Class<? extends Measurement> measurementType,
            SimpleVehicleMessage message)
                throws UnrecognizedMeasurementTypeException, NoValueException {
        Constructor<? extends Measurement> constructor;
        if(message == null) {
            throw new NoValueException();
        }

        try {
            Measurement measurement;
            SimpleVehicleMessage simpleMessage = message.asSimpleMessage();
            Class<?> valueClass = simpleMessage.getValue().getClass();
            if(valueClass == Double.class || valueClass == Integer.class) {
                valueClass = Number.class;
            }

            if(message instanceof EventedSimpleVehicleMessage) {
                EventedSimpleVehicleMessage eventedMessage =
                        message.asEventedMessage();

                Class<?> eventClass = eventedMessage.getEvent().getClass();
                if(eventClass == Double.class || eventClass == Integer.class) {
                    eventClass = Number.class;
                }

                try {
                    constructor = measurementType.getConstructor(
                            valueClass, eventClass);
                } catch(NoSuchMethodException e) {
                    throw new UnrecognizedMeasurementTypeException(
                            measurementType +
                            " doesn't have the expected constructor, " +
                           measurementType + "(" +
                           valueClass + ", " + eventClass + ")");
                }

                measurement = constructor.newInstance(
                        eventedMessage.getValue(),
                        eventedMessage.getEvent());
            } else {
                try {
                    constructor = measurementType.getConstructor(valueClass);
                } catch(NoSuchMethodException e) {
                    throw new UnrecognizedMeasurementTypeException(
                            measurementType +
                            " doesn't have the expected constructor, " +
                           measurementType + "(" +
                           valueClass + ")");
                }

                measurement = constructor.newInstance(
                        simpleMessage.getValue());
            }

            if (simpleMessage.getTimestamp() != null) {
                measurement.setTimestamp(simpleMessage.getTimestamp());
            }
            // https://github.com/openxc/openxc-android/issues/185
            return measurement;
        } catch(InstantiationException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " is abstract", e);
        } catch(IllegalAccessException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " has a private constructor", e);
        } catch(IllegalArgumentException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + " has unexpected arguments", e);
        } catch(InvocationTargetException e) {
            throw new UnrecognizedMeasurementTypeException(
                    measurementType + "'s constructor threw an exception",
                    e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null) {
            return false;
        }

        if(getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final BaseMeasurement<TheUnit> other = (BaseMeasurement<TheUnit>) obj;
        return Objects.equal(getValue(), other.getValue()) &&
            Objects.equal(other.getRange(), getRange());
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
