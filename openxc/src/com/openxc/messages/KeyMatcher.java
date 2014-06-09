package com.openxc.messages;

public abstract class KeyMatcher {
    public abstract boolean matches(KeyedMessage other);

    public static KeyMatcher buildExactMatcher(final KeyedMessage keyedObject) {
        return new KeyMatcher() {
            public boolean matches(KeyedMessage other) {
                return keyedObject.equals(other);
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
