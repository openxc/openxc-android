package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class KeyMatcherTest {
    KeyedMessage keyed = new TestKeyedMessage();
    KeyedMessage different = new KeyedMessage() {
        @Override
        public MessageKey getKey() {
            HashMap<String, Object> key = new HashMap<>();
            key.put("baz", "bing");
            return new MessageKey(key);
        }
    };

    @Test
    public void exactMatcherMatchesOriginal() {
        KeyMatcher matcher = ExactKeyMatcher.buildExactMatcher(keyed);
        assertTrue(matcher.matches(keyed));
    }

    @Test
    public void exactMatcherMatchesMessageWithSameKey() {
        KeyMatcher matcher = ExactKeyMatcher.buildExactMatcher(keyed);

        KeyedMessage another = new TestKeyedMessage();
        assertTrue(matcher.matches(another));
    }

    @Test
    public void wildcardMatcherMatchesAll() {
        assertTrue(KeyMatcher.getWildcardMatcher().matches(keyed));
    }

    @Test
    public void wildcardEqualsItself() {
        assertThat(KeyMatcher.getWildcardMatcher(),
                equalTo(KeyMatcher.getWildcardMatcher()));
    }

    @Test
    public void wildcardDoesNotEqualOther() {
        KeyMatcher wildcard = KeyMatcher.getWildcardMatcher();
        KeyMatcher exact = ExactKeyMatcher.buildExactMatcher(keyed);
        assertThat(wildcard, not(equalTo(exact)));
    }

    @Test
    public void exactMatcherMatchesAnotherExactMatcher() {
        KeyedMessage another = new TestKeyedMessage();
        assertEquals(ExactKeyMatcher.buildExactMatcher(keyed),
                ExactKeyMatcher.buildExactMatcher(another));
    }

    @Test
    public void exactMatcherDoesntMatchDifferentExactMatcher() {
        assertThat(ExactKeyMatcher.buildExactMatcher(keyed),
                not(equalTo(ExactKeyMatcher.buildExactMatcher(different))));
    }

    @Test
    public void sameHashCode() {
        assertEquals(ExactKeyMatcher.buildExactMatcher(keyed).hashCode(),
                ExactKeyMatcher.buildExactMatcher(keyed).hashCode());
    }

    @Test
    public void inexactHashCodeNotEqual() {
        KeyMatcher matcher = new KeyMatcher() {
            @Override
            public boolean matches(MessageKey message) {
                return false;
            }
        };

        KeyMatcher anotherMatcher = new KeyMatcher() {
            @Override
            public boolean matches(MessageKey message) {
                return false;
            }
        };

        assertThat(matcher.hashCode(), not(equalTo(anotherMatcher.hashCode())));
    }

    @Test
    public void differentHashCode() {
        assertThat(ExactKeyMatcher.buildExactMatcher(keyed).hashCode(),
                not(equalTo(ExactKeyMatcher.buildExactMatcher(
                            different).hashCode())));
    }

    @Test
    public void nullNotEqualToExact() {
        KeyMatcher matcher = ExactKeyMatcher.buildExactMatcher(keyed);
        assertFalse(matcher.equals(null));
    }

    @Test
    public void nullNotEqual() {
        KeyMatcher matcher = new KeyMatcher() {
            @Override
            public boolean matches(MessageKey message) {
                return false;
            }
        };
        assertFalse(matcher.equals(null));
    }

    @Test
    public void toStringNotNull() {
        assertThat(keyed.toString(), notNullValue());
    }

    private class TestKeyedMessage extends KeyedMessage {
        @Override
        public MessageKey getKey() {
            HashMap<String, Object> key = new HashMap<>();
            // This is the same key as the other, but not the same instance
            // of the KeyedMessage
            key.put("foo", "bar");
            return new MessageKey(key);
        }
    }
}
