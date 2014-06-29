package com.openxc.messages;

public abstract class ExactKeyMatcher extends KeyMatcher {
    public abstract MessageKey getKey();

    public static ExactKeyMatcher buildExactMatcher(final KeyedMessage keyed) {
        return new ExactKeyMatcher() {
            private MessageKey mKey = keyed.getKey();

            public boolean matches(KeyedMessage other) {
                return mKey.equals(other.getKey());
            }

            public MessageKey getKey() {
                return mKey;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        return getKey().equals(((ExactKeyMatcher)obj).getKey());
    }
}
