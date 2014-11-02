package com.openxc.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import com.openxc.TestUtils;
import com.openxc.units.Degree;

public class AgingDataTest {
    AgingData<Degree> data;
    Degree value;

    @Before
    public void setUp() {
        value = new Degree(0.0);
        data = new AgingData<Degree>(value);
    }

    @Test
    public void testBornNow() {
        TestUtils.pause(10);
        assertThat(data.getAge(), greaterThan(Long.valueOf(0)));
    }

    @Test
    public void testBornEarlier() {
        Date otherTime = new Date(data.getAge() + 100);
        data = new AgingData<Degree>(otherTime, value);
        assertThat(data.getAge(), greaterThanOrEqualTo(Long.valueOf(100)));
    }

    @Test
    public void testSetOverride() {
        assertThat(data.getAge(), lessThanOrEqualTo(Long.valueOf(1)));
        data.setTimestamp(data.getAge() + 100);
        assertThat(data.getAge(), greaterThanOrEqualTo(Long.valueOf(100)));
    }

    @Test
    public void setInvalidTimstampIgnored() {
        long timestamp = data.getTimestamp();
        data.setTimestamp(0);
        assertEquals(timestamp, data.getTimestamp());
    }

    @Test
    public void testToStringNotNull() {
        assertThat(data.toString(), notNullValue());
    }
}
