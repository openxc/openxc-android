package com.openxc.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;

public class RangeTest extends TestCase {
    Range<Double> range;

    @Override
    public void setUp() {
        range = new Range<Double>(0.0, 100.1);
    }

    public void testMin() {
        assertThat(range.getMin(), equalTo(0.0));
    }

    public void testMax() {
        assertThat(range.getMax(), equalTo(100.1));
    }
}
