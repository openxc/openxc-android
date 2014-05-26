package com.openxc.util;

import java.util.Date;

import com.openxc.TestUtils;

import com.openxc.units.Degree;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.openxc.util.AgingData;

public class AgingDataTest extends TestCase {
    AgingData<Degree> data;
    Degree value = new Degree(0.0);

    public void testBornNow() {
        data = new AgingData<Degree>(value);
        TestUtils.pause(10);
        assertThat(data.getAge(), greaterThan(Long.valueOf(0)));
    }

    public void testBornEarlier() {
        data = new AgingData<Degree>(value);
        Date otherTime = new Date(data.getAge() + 100);
        data = new AgingData<Degree>(otherTime, value);
        assertThat(data.getAge(), greaterThanOrEqualTo(Long.valueOf(100)));
    }

    public void testSetOverride() {
        data = new AgingData<Degree>(value);
        assertThat(data.getAge(), lessThanOrEqualTo(Long.valueOf(1)));
        data.setTimestamp(data.getAge() + 100);
        assertThat(data.getAge(), greaterThanOrEqualTo(Long.valueOf(100)));
    }
}
