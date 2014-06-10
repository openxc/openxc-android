package com.openxc.messages;

import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import junit.framework.TestCase;
import org.junit.*;
import static org.junit.Assert.*;

import android.os.Parcel;

public class KeyMatcherTest {
    KeyedMessage keyed = new KeyedMessage() {
        public MessageKey getKey() {
            HashMap<String, Object> key = new HashMap<>();
            key.put("foo", "bar");
            return new MessageKey(key);
        }
    };

    @Test
    public void exactMatcherMatchesOriginal() {
        KeyMatcher matcher = KeyMatcher.buildExactMatcher(keyed);
        assertTrue(matcher.matches(keyed));
    }

    @Test
    public void exactMatcherMatchesAnotherWithSameKey() {
        KeyMatcher matcher = KeyMatcher.buildExactMatcher(keyed);

        KeyedMessage another = new KeyedMessage() {
            public MessageKey getKey() {
                HashMap<String, Object> key = new HashMap<>();
                key.put("foo", "bar");
                return new MessageKey(key);
            }
        };
        assertTrue(matcher.matches(another));
    }

    @Test
    public void wildcardMatcherMatchesAll() {
        KeyMatcher matcher = KeyMatcher.buildExactMatcher(keyed);
        assertTrue(KeyMatcher.getWildcardMatcher().matches(keyed));
    }
}
