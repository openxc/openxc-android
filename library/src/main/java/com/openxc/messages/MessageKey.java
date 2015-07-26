package com.openxc.messages;

import com.google.common.base.Objects;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A MessageKey is an identifying key for a VehicleMessage.
 *
 * The key is a simple map of string names to arbitrary objects.
 *
 * This is used to filter incoming messages to send them to the proper
 * listeners.
 */
public class MessageKey implements Parcelable {
    private Map<String, Object> mParts = new HashMap<>();
    private int mHashCode;

    public MessageKey(Map<String, Object> parts) {
        mParts = parts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        // Take advantage of the fact that we likely already calculated and
        // cached the hash code and use that for the equality comparison.
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        if(mHashCode == 0) {
            mHashCode = Objects.hashCode(mParts);
        }
        return mHashCode;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeMap(mParts);
    }

    protected void readFromParcel(Parcel in) {
        in.readMap(mParts, null);
    }

    public static final Parcelable.Creator<MessageKey> CREATOR =
            new Parcelable.Creator<MessageKey>() {
        @Override
        public MessageKey createFromParcel(Parcel in) {
            return new MessageKey(in);
        }

        @Override
        public MessageKey[] newArray(int size) {
            return new MessageKey[size];
        }
    };

    private MessageKey(Parcel in) {
        readFromParcel(in);
    }
}
