package com.openxc.messages;

public abstract class KeyMatcher {
    public abstract boolean matches(KeyedMessage other);

    public static KeyMatcher buildExactMatcher(final KeyedMessage keyed) {
        return new KeyMatcher() {
            public boolean matches(KeyedMessage other) {
                return keyed.getKey().equals(other.getKey());
            }
        };
    }

    private static KeyMatcher sWildcardMatcher = new KeyMatcher() {
        public boolean matches(KeyedMessage other) {
            return true;
        }
    };

    public static KeyMatcher getWildcardMatcher() {
        return sWildcardMatcher;
    }
}
