package com.openxc.messages;

public abstract class KeyMatcher {
    public abstract boolean matches(KeyedMessage other);
    public abstract KeyedMessage get();

    public static KeyMatcher buildExactMatcher(final KeyedMessage keyed) {
        return new KeyMatcher() {
            public boolean matches(KeyedMessage other) {
                return keyed.getKey().equals(other.getKey());
            }
            public KeyedMessage get() {
                return keyed;
            }
        };
    }

    private static KeyMatcher sWildcardMatcher = new KeyMatcher() {
        public boolean matches(KeyedMessage other) {
            return true;
        }
        public KeyedMessage get() {
            return null;
        }
    };

    public static KeyMatcher getWildcardMatcher() {
        return sWildcardMatcher;
    }
}
