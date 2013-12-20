package com.openxc.measurements;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.openxc.NoValueException;
import com.openxc.remote.RawMeasurement;
import com.openxc.units.Unit;
import com.openxc.util.AgingData;
import com.openxc.util.Range;

/**
 * The BaseMeasurement is the base implementation of the Measurement, and
 * wraps wraps an instance of a {@link Unit}, and the value it returns is always
 * in terms of this Unit.
 *
 * The Unit wrapper might seem annoying at first, but it is critical to avoid
 * misinterpreting the unit and crashing your lander into Mars
 * (http://en.wikipedia.org/wiki/Mars_Climate_Orbiter).
 *
 * Most applications will not use this class directly, but will import specific
 * child classes that correspond to specific types of measurements - i.e. the
 * parameterized instances of this class with a Unit. That may seem like a
 * "psuedo-typedef" but we're using it it to enforce the binding between
 * the measurement and its unit type. This unfortunately means we have to add
 * constructors to every child class because they aren't inherited from
 * Measurement. If you know of a better way, please say so.
 */
public class BaseMeasurement<TheUnit extends Unit> implements Measurement {
    private AgingData<TheUnit> mValue;
    private AgingData<Unit> mEvent;
    private Range<TheUnit> mRange;
    private static BiMap<String, Class<? extends Measurement>>
            sMeasurementIdToClass;

    static {
        sMeasurementIdToClass = HashBiMap.create();
    }

    /**
     * Construct a new Measurement with the given value.
     *
     * @param value the TheUnit this measurement represents.
     */
    public BaseMeasurement(TheUnit value) {
        mValue = new AgingData<TheUnit>(value);
    }

    public BaseMeasurement(TheUnit value, Unit event) {
        this(value);
        mEvent = new AgingData<Unit>(event);
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

    public void setTimestamp(long timestamp) {
        mValue.setTimestamp(timestamp);
    }

    public long getAge() {
        return mValue.getAge();
    }

    public long getBirthtime() {
        return mValue.getTimestamp();
    }

    public boolean hasRange() {
        return mRange != null;
    }

    public Range<TheUnit> getRange() throws NoRangeException {
        if(!hasRange()) {
            throw new NoRangeException();
        }
        return mRange;
    }

    public TheUnit getValue() {
        return mValue.getValue();
    }

    public Object getEvent() {
        if(mEvent != null) {
            return mEvent.getValue();
        }
        return null;
    }

    public Object getSerializedValue() {
        return getValue().getSerializedValue();
    }

    public Object getSerializedEvent() {
        return getEvent();
    }

    public String serialize() {
        return toRaw().serialize();
    }

    public RawMeasurement toRaw() {
        return new RawMeasurement(getGenericName(), getSerializedValue(),
                getSerializedEvent(), mValue.getTimestamp());
    }

    public static Measurement deserialize(String measurementString)
            throws NoValueException, UnrecognizedMeasurementTypeException {
        RawMeasurement rawMeasurement = new RawMeasurement(measurementString);
        return BaseMeasurement.getMeasurementFromRaw(rawMeasurement);
    }

    public String getGenericName() {
        return "base_measurement";
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

    public static String getIdForClass(
            Class<? extends Measurement> measurementType)
            throws UnrecognizedMeasurementTypeException {
        if(!sMeasurementIdToClass.inverse().containsKey(measurementType)) {
            cacheMeasurementId(measurementType);
        }
        return sMeasurementIdToClass.inverse().get(measurementType);
    }

    public static Class<? extends Measurement>
            getClassForId(String measurementId)
            throws UnrecognizedMeasurementTypeException {
        Class<? extends Measurement> result = sMeasurementIdToClass.get(measurementId);
        if(result == null) {
            throw new UnrecognizedMeasurementTypeException(
                    "Didn't have a measurement with ID " + measurementId +
                    " cached");
        }
        return result;
    }

    public static Measurement getMeasurementFromRaw(
            RawMeasurement rawMeasurement)
            throws UnrecognizedMeasurementTypeException, NoValueException {
        Class<? extends Measurement> measurementClass =
            BaseMeasurement.getClassForId(rawMeasurement.getName());
        return BaseMeasurement.getMeasurementFromRaw(measurementClass,
                rawMeasurement);
    }

    public static Measurement getMeasurementFromRaw(
            Class<? extends Measurement> measurementType,
            RawMeasurement rawMeasurement)
            throws UnrecognizedMeasurementTypeException, NoValueException {
        Constructor<? extends Measurement> constructor = null;
        if(rawMeasurement != null && rawMeasurement.getValue() != null) {
            Class<?> valueClass = rawMeasurement.getValue().getClass();
            if(valueClass == Double.class || valueClass == Integer.class) {
                valueClass = Number.class;
            }

            Class<?> eventClass = rawMeasurement.hasEvent() ?
                                rawMeasurement.getEvent().getClass()
                                : null;
            if(eventClass == Double.class || eventClass == Integer.class) {
                eventClass = Number.class;
            }

            try {
                if(eventClass != null) {
                    constructor = measurementType.getConstructor(
                            valueClass, eventClass);
                } else {
                    constructor = measurementType.getConstructor(valueClass);
                }
            } catch(NoSuchMethodException e) {
                throw new UnrecognizedMeasurementTypeException(measurementType +
                        " doesn't have the expected constructor, " +
                       measurementType + "(" +
                       valueClass +
                       (eventClass != null ? ", " + eventClass : "") + ")");
            }

            Measurement measurement;
            try {
                if(eventClass != null) {
                    measurement = constructor.newInstance(
                            rawMeasurement.getValue(),
                            rawMeasurement.getEvent());
                } else {
                    measurement = constructor.newInstance(
                            rawMeasurement.getValue());
                }
                measurement.setTimestamp(rawMeasurement.getTimestamp());
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
        throw new NoValueException();
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
        if(!other.getValue().equals(getValue())) {
            return false;
        }

        if(other.getEvent() != null && getEvent() != null) {
            if(!other.getEvent().equals(getEvent())) {
                return false;
            }
        } else if(other.getEvent() != getEvent()) {
            return false;
        }

        if(other.hasRange() != hasRange()) {
            return false;
        } else {
            try {
                if(!other.getRange().equals(getRange())) {
                    return false;
                }
            } catch(NoRangeException e) { }
        }

        return true;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
