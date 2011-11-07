package com.openxc.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import com.openxc.util.Range;

public class RangeTest {
    Range<Double> range;

    @Before
    public void setUp() {
        range = new Range<Double>(0.0, 100.1);
    }

    @Test
    public void testMin() {
        assertThat(range.getMin(), equalTo(0.0));
    }

    @Test
    public void testMax() {
        assertThat(range.getMax(), equalTo(100.1));
    }
}
