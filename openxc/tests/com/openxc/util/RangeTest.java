package com.openxc.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Test;

public class RangeTest {
    Range<Double> range;

    @Before
    public void setUp() {
        range = new Range<Double>(0.0, 100.1);
    }

    @Test
    public void getMin() {
        assertThat(range.getMin(), equalTo(0.0));
    }

    @Test
    public void getMax() {
        assertThat(range.getMax(), equalTo(100.1));
    }

    @Test
    public void sameEquals() {
        assertThat(range, equalTo(range));
    }

    @Test
    public void sameMinMaxEquals() {
        Range<Double> another = new Range<Double>(range.getMin(), range.getMax());
        assertThat(range, equalTo(another));
    }

    @Test
    public void differentNotEqual() {
        Range<Double> another = new Range<Double>(range.getMin() + 1, range.getMax());
        assertThat(range, not(equalTo(another)));

        another = new Range<Double>(range.getMin(), range.getMax() + 1);
        assertThat(range, not(equalTo(another)));

        another = new Range<Double>(range.getMin() + 1, range.getMax() + 1);
        assertThat(range, not(equalTo(another)));
    }

    @Test
    public void toStringNotNull() {
        assertThat(range.toString(), notNullValue());
    }
}
