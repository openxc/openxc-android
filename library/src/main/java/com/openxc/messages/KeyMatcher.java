package com.openxc.messages;

/**
 * A KeyMatcher is used to filter incoming vehicle messages to decide which
 * should be passed on to registered listeners.
 *
 * For example, a key matcher may match command responses for a particular
 * command request.
 */
public abstract class KeyMatcher {
    public abstract boolean matches(MessageKey key);

    /**
     * Return true if the message's key matches this key matcher.
     */
    public boolean matches(KeyedMessage other) {
        return matches(other.getKey());
    }

    /**
     * Return a shared instance of a KeyMatcher that matches anything.
     */
    public static KeyMatcher getWildcardMatcher() {
        return sWildcardMatcher;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj == this;
    }

    private static KeyMatcher sWildcardMatcher = new KeyMatcher() {
        @Override
        public boolean matches(MessageKey other) {
            return true;
        }
    };
}
