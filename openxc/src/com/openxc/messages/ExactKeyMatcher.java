package com.openxc.messages;

import com.google.common.base.Objects;

public abstract class ExactKeyMatcher extends KeyMatcher {
    public abstract MessageKey getKey();

    public static ExactKeyMatcher buildExactMatcher(final MessageKey key) {
        return new ExactKeyMatcher() {
            private MessageKey mKey = key;

            public boolean matches(MessageKey other) {
                return mKey.equals(other);
            }

            public MessageKey getKey() {
                return mKey;
            }
        };
    }

    public static ExactKeyMatcher buildExactMatcher(final KeyedMessage keyed) {
        return buildExactMatcher(keyed.getKey());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        return getKey().equals(((ExactKeyMatcher)obj).getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getKey());
    }
}
