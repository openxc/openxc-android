package com.openxc.messages;

public abstract class KeyMatcher {
    public abstract boolean matches(MessageKey other);

    public static KeyMatcher buildExactMatcher(final MessageKey messageKey) {
        return new KeyMatcher() {
            public boolean matches(MessageKey other) {
                return messageKey.equals(other);
            }
        };
    }

    private static KeyMatcher sWildcardMatcher = new KeyMatcher() {
        public boolean matches(MessageKey other) {
            return true;
        }
    };

    public static KeyMatcher getWildcardMatcher() {
        return sWildcardMatcher;
    }
}
