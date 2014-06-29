package com.openxc.messages;

import com.google.common.base.Objects;

public abstract class KeyMatcher {
    public abstract boolean matches(MessageKey key);

    public boolean matches(KeyedMessage other) {
        return matches(other.getKey());
    }

    public static KeyMatcher getWildcardMatcher() {
        return sWildcardMatcher;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        return obj == this;
    }

    private static KeyMatcher sWildcardMatcher = new KeyMatcher() {
        public boolean matches(MessageKey other) {
            return true;
        }
    };
}
