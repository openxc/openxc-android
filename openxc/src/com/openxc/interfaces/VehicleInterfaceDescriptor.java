package com.openxc.interfaces;

import com.google.common.base.Objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import static com.google.common.base.MoreObjects.toStringHelper;

public class VehicleInterfaceDescriptor implements Parcelable {
    private final static String TAG =
            VehicleInterfaceDescriptor.class.getName();
    private boolean mConnected;
    private Class<? extends VehicleInterface> mInterfaceClass;

    public VehicleInterfaceDescriptor(
            Class<? extends VehicleInterface> interfaceClass,
            boolean connected) {
        mInterfaceClass = interfaceClass;
        mConnected = connected;
    }

    public VehicleInterfaceDescriptor(VehicleInterface vi) {
        this(vi.getClass(), vi.isConnected());
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
        return Objects.equal(mConnected, other.mConnected) &&
                Objects.equal(mInterfaceClass, other.mInterfaceClass);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mConnected, mInterfaceClass);
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
            mInterfaceClass = VehicleInterfaceFactory.findClass(
                    in.readString());
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
