package com.openxc.messages;

public abstract class KeyMatcher {
    public abstract boolean matches(KeyedMessage other);

    private static KeyMatcher sWildcardMatcher = new KeyMatcher() {
        public boolean matches(KeyedMessage other) {
            return true;
        }
    };

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
}
