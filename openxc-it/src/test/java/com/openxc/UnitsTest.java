package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openxc.units.Meter;

public class UnitsTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testComparableToDouble() {
        Meter value = new Meter(10);
        assertThat(value.doubleValue(), equalTo(10.0));
    }
}
