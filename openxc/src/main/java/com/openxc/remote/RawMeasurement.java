package com.openxc.remote;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * An untyped measurement used only for the AIDL RemoteVehicleService interface.
 *
 * All OpenXC measurements need to be representable by a double so they can be
 * easily fit through the AIDL interface to RemoteVehicleService. This class
 * shouldn't be used anywhere else becuase hey, types are important.
 *
 * This class implements the Parcelable interface, so it can be used directly as
 * a return value or function parameter in an AIDL interface.
 *
 * @see com.openxc.measurements.Measurement
 */
public class RawMeasurement extends AbstractRawMeasurement<Double, Double>
        implements Parcelable {
    private static final String TAG = "RawMeasurement";

    public static final Parcelable.Creator<RawMeasurement> CREATOR =
            new Parcelable.Creator<RawMeasurement>() {
        public RawMeasurement createFromParcel(Parcel in) {
            return new RawMeasurement(in);
        }

        public RawMeasurement[] newArray(int size) {
            return new RawMeasurement[size];
        }
    };

    public RawMeasurement() {
        super();
    }

    public RawMeasurement(Double value) {
        super(value);
    }

    /**
     * The value is cast to a Double, with no loss of precision.
     *
     * @param value The Integer value of the element.
     */
    public RawMeasurement(Integer value) {
        this(new Double(value));
    }

    /**
     * The boolean is cast to a double (1 for true, 0 for false with no loss of
     * precision.
     *
     * @param value The Boolean value of the element.
     */
    public RawMeasurement(Boolean value) {
        this(RawMeasurement.booleanToDouble(value));
    }

    /**
     * The value is converted to a Double equal to the hash of the string.
     *
     * @param value The String value of the element.
     */
    public RawMeasurement(String value) {
        this(new Double(value.toUpperCase().hashCode()));
    }

    /**
     * The value and event are converted to Doubles equal to the hash of the
     * strings.
     *
     * @param value The String value of the element.
     * @param event The String event of the element.
     */
    public RawMeasurement(String value, String event) {
        this(new Double(value.toUpperCase().hashCode()),
               new Double(event.toUpperCase().hashCode()));
    }

    /**
     * The value and event are converted to Doubles equal to the hash of the
     * strings.
     *
     * @param value The String value of the element.
     * @param event The Boolean event of the element.
     */
    public RawMeasurement(String value, Boolean event) {
        this(new Double(value.toUpperCase().hashCode()),
                RawMeasurement.booleanToDouble(event));
    }

    public RawMeasurement(Double value, Double event) {
        super(value, event);
    }

    private RawMeasurement(Parcel in) {
        readFromParcel(in);
    }

    public boolean isValid() {
        return super.isValid() && !getValue().isNaN();
    }

    public boolean hasEvent() {
        return super.hasEvent() && !getEvent().isNaN();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(getValue().doubleValue());
        if(getEvent() != null) {
            out.writeDouble(getEvent().doubleValue());
        } else {
            out.writeDouble(Double.NaN);
        }
    }

    public void readFromParcel(Parcel in) {
        setValue(new Double(in.readDouble()));
        setEvent(new Double(in.readDouble()));
    }

    public static RawMeasurement measurementFromObjects(Object value) {
        return RawMeasurement.measurementFromObjects(value, null);
    }

    public static RawMeasurement measurementFromObjects(Object value,
            Object event) {
        Constructor<RawMeasurement> constructor;
        try {
            if(event != null) {
                constructor = RawMeasurement.class.getConstructor(
                        value.getClass(), event.getClass());
            } else {
                constructor = RawMeasurement.class.getConstructor(
                        value.getClass());
            }
        } catch(NoSuchMethodException e) {
            String logMessage = "Received data of an unsupported type " +
                "from the data source: " + value + ", a " +
                value.getClass();
            if(event != null) {
                logMessage += " and event " + event + ", a " +
                    event.getClass();
            }
            Log.w(TAG, logMessage);
            return null;
        }

        RawMeasurement measurement = null;
        try {
            if(event != null) {
                measurement = constructor.newInstance(value, event);
            } else {
                measurement = constructor.newInstance(value);
            }
        } catch(InstantiationException e) {
            Log.w(TAG, "Couldn't instantiate raw measurement", e);
        } catch(IllegalAccessException e) {
            Log.w(TAG, "Constructor is not accessible", e);
        } catch(InvocationTargetException e) {
            Log.w(TAG, "Constructor threw an exception", e);
        }
        return measurement;
    }

    private static Double booleanToDouble(Boolean value) {
        return new Double(value.booleanValue() ? 1 : 0);
    }
}
