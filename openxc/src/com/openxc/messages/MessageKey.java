package com.openxc.messages;

import java.util.Map;
import java.util.HashMap;

import com.google.common.base.Objects;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageKey implements Parcelable {
    private Map<String, Object> mParts = new HashMap<>();

    public MessageKey(Map<String, Object> parts) {
        mParts = parts;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        final MessageKey other = (MessageKey) obj;
        return mParts.equals(other.mParts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mParts);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeMap(mParts);
    }

    protected void readFromParcel(Parcel in) {
        in.readMap(mParts, null);
    }

    public static final Parcelable.Creator<MessageKey> CREATOR =
            new Parcelable.Creator<MessageKey>() {
        public MessageKey createFromParcel(Parcel in) {
            return new MessageKey(in);
        }

        public MessageKey[] newArray(int size) {
            return new MessageKey[size];
        }
    };

    private MessageKey(Parcel in) {
        readFromParcel(in);
    }
}
