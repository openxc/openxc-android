package com.ford.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ford.openxc.units.Meter;
import com.ford.openxc.measurements.Measurement;

public class MeasurementTest {
    Measurement<Meter> measurement;

    @Before
    public void setUp() {
        measurement = new Measurement<Meter>();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEmpty() {
        assertThat(measurement.hasValue(), equalTo(true));
    }

    @Test
    public void testEmptyAge() {
        assertThat(measurement.getAge(), equalTo(0));
    }

}
