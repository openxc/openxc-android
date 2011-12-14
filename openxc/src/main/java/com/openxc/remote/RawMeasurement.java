package com.openxc.remote;

import android.os.Parcel;
import android.os.Parcelable;

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
}
