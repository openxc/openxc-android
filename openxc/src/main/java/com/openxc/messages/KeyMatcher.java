package com.openxc.messages;


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
        @Override
        public boolean matches(MessageKey other) {
            return true;
        }
    };
}
