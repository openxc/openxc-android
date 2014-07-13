package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.openxc.units.Boolean;

public class BooleanTest {
    Boolean value;

    @Before
    public void setUp() {
        value = new Boolean(new Integer(1));
    }

    @Test
    public void constructFromNumber() {
        assertTrue(value.booleanValue());
        value = new Boolean(new Integer(0));
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
