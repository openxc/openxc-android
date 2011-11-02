package com.ford.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MeasurementTest {
    Measurement measurement;

    @Before
    public void setUp() {
        measurement = new Measurement<Meter>();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEmpty() {
        assertThat(not(measurement.hasValue()));
    }

    @Test
    public void testEmptyAge() {
        assertThat(measurement.getAge(), equalTo(0));
    }

}
