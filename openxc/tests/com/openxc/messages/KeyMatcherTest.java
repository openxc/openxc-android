package com.openxc.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class KeyMatcherTest {
    KeyedMessage keyed = new TestKeyedMessage();

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
        KeyMatcher matcher = ExactKeyMatcher.buildExactMatcher(keyed);
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
        KeyedMessage another = new KeyedMessage() {
            public MessageKey getKey() {
                HashMap<String, Object> key = new HashMap<>();
                key.put("baz", "bing");
                return new MessageKey(key);
            }
        };

        assertThat(ExactKeyMatcher.buildExactMatcher(keyed),
                not(equalTo(ExactKeyMatcher.buildExactMatcher(another))));
    }

    private class TestKeyedMessage extends KeyedMessage {
        public MessageKey getKey() {
            HashMap<String, Object> key = new HashMap<>();
            // This is the same key as the other, but not the same instance
            // of the KeyedMessage
            key.put("foo", "bar");
            return new MessageKey(key);
        }
    }
}
