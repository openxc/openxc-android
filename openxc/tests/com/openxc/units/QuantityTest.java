package com.openxc.units;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import com.openxc.units.Kilometer;

public class QuantityTest {
    @Test
    public void testStringContainsTypeString() {
        Kilometer value = new Kilometer(Integer.valueOf(42));
        assertThat(value.toString(), containsString(value.getTypeString()));
    }
}
