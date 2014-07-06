package com.openxc.interfaces;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.network.NetworkVehicleInterface;
import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.sources.VehicleDataSource;
import com.openxc.sources.trace.TraceVehicleDataSource;

public class VehicleInterfaceDescriptor implements Parcelable {
    private final static String TAG = VehicleInterfaceDescriptor.class.getName();
    private boolean mConnected;
    private Class<? extends VehicleInterface> mInterfaceClass;

    public VehicleInterfaceDescriptor(
            Class<? extends VehicleInterface> interfaceClass,
            boolean connected) {
        mInterfaceClass = interfaceClass;
        mConnected = connected;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public Class<? extends VehicleInterface> getInterfaceClass() {
        return mInterfaceClass;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        final VehicleInterfaceDescriptor other =
                (VehicleInterfaceDescriptor) obj;
        return Objects.equals(mConnected, other.mConnected) &&
                Objects.equals(mInterfaceClass, other.mInterfaceClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mConnected, mInterfaceClass);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("class", mInterfaceClass)
            .add("connected", mConnected)
            .toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mConnected ? 1 : 0);
        out.writeString(mInterfaceClass.getName());
    }

    protected void readFromParcel(Parcel in) {
        mConnected = in.readInt() == 1;
        try {
            mInterfaceClass = VehicleInterfaceFactory.findClass(in.readString());
        } catch(VehicleInterfaceException e) {
            Log.w(TAG, "Unable to load class for vehicle interface by name", e);
        }
    }

    public static final Parcelable.Creator<VehicleInterfaceDescriptor> CREATOR =
            new Parcelable.Creator<VehicleInterfaceDescriptor>() {
        @Override
        public VehicleInterfaceDescriptor createFromParcel(Parcel in) {
            return new VehicleInterfaceDescriptor(in);
        }

        @Override
        public VehicleInterfaceDescriptor[] newArray(int size) {
            return new VehicleInterfaceDescriptor[size];
        }
    };

    private VehicleInterfaceDescriptor(Parcel in) {
        readFromParcel(in);
    }
}
