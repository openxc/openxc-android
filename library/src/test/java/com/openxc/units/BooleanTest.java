package com.openxc.units;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import com.openxc.units.Boolean;

public class BooleanTest {
    Boolean value;

    @Before
    public void setUp() {
        value = new Boolean(Integer.valueOf(1));
    }

    @Test
    public void constructFromNumber() {
        assertTrue(value.booleanValue());
        value = new Boolean(Integer.valueOf(0));
        assertFalse(value.booleanValue());
    }

    @Test
    public void toStringNotNull() {
        assertThat(value.toString(), instanceOf(String.class));
    }

    @Test
    public void sameEquals() {
        assertThat(value, equalTo(value));
    }

    @Test
    public void sameValueEquals() {
        assertThat(value, equalTo(new Boolean(true)));
    }

    @Test
    public void differentValueNotEquals() {
        assertThat(value, not(equalTo(new Boolean(false))));
    }
}
