package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.measurements.BaseMeasurement;
import com.openxc.units.Meter;

@RunWith(RobolectricTestRunner.class)
public class NoRangeMeasurementTest {
    BaseMeasurement<Meter> measurement;

    @Before
    public void setUp() {
        measurement = new BaseMeasurement<Meter>(new Meter(10.0)) {
            public String getGenericName() {
                return "foo";
            }
        };
    }

    @Test
    public void hasNoRange() {
        assertFalse(measurement.hasRange());
    }

    @Test
    public void emptyRange() {
        assertThat(measurement.getRange(), nullValue());
    }

    @Test
    public void get() {
        assertThat(measurement.getValue().doubleValue(), equalTo(10.0));
    }
}
