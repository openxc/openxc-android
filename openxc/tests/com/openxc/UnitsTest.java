package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import junit.framework.TestCase;

import com.openxc.units.Meter;

public class UnitsTest extends TestCase {
    public void testComparableToDouble() {
        Meter value = new Meter(10);
        assertThat(value.doubleValue(), equalTo(10.0));
    }
}
