package com.openxc.messages;

import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import junit.framework.TestCase;
import org.junit.*;
import static org.junit.Assert.*;

import android.os.Parcel;

public class KeyMatcherTest {

    public MessageKey getKey() {
       HashMap<String, Object> key = new HashMap<>();
       key.put("foo", "bar");
       return new MessageKey(key);
    }

    @Test
    public void exactMatcherMatchesOriginal() {
        KeyMatcher matcher = KeyMatcher.buildExactMatcher(getKey());
        assertTrue(matcher.matches(getKey()));
    }

    @Test
    public void wildcardMatcherMatchesAll() {
        assertTrue(KeyMatcher.getWildcardMatcher().matches(getKey()));
    }
}
