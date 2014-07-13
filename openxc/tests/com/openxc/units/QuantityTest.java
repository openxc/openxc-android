package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.openxc.units.Kilometer;

public class QuantityTest {
    @Test
    public void testStringContainsTypeString() {
        Kilometer value = new Kilometer(new Integer(42));
        assertThat(value.toString(), containsString(value.getTypeString()));
    }
}
