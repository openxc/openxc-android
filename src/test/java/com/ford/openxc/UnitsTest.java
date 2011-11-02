package com.ford.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        assertThat(value, equalTo(10));
    }
}
