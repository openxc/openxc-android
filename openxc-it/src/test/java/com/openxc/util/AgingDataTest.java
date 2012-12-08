package com.openxc.util;

import java.util.Calendar;
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
        assertThat(data.getAge(), greaterThan(0.0));
    }

    public void testBornEarlier() {
        data = new AgingData<Degree>(value);
        Calendar otherTime = Calendar.getInstance();
        otherTime.setTimeInMillis((long)(data.getAge() + 100) * 1000);
        data = new AgingData<Degree>(otherTime.getTime(), value);
        assertThat(data.getAge(), greaterThanOrEqualTo(100.0));
    }

    public void testSetOverride() {
        data = new AgingData<Degree>(value);
        assertThat(data.getAge(), lessThanOrEqualTo(1.0));
        data.setTimestamp(data.getAge() + 100);
        assertThat(data.getAge(), greaterThanOrEqualTo(100.0));
    }
}
