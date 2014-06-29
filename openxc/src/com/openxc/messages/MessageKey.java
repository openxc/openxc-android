package com.openxc.messages;

import java.util.Map;

import com.google.common.base.Objects;

public class MessageKey {
    private Map<String, Object> mParts;

    public MessageKey(Map<String, Object> parts) {
        mParts = parts;
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
}
