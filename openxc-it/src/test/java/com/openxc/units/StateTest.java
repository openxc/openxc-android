package com.openxc.units;

import junit.framework.TestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class StateTest extends TestCase {
    State<TestState> state;

    private enum TestState {
        ON,
        OFF,
        PEANUT_BUTTER;
    }

    @Override
    public void setUp() {
        state = new State<TestState>(TestState.PEANUT_BUTTER);
    }

    public void testSeralizedValue() {
        assertThat(state.getSerializedValue(), equalTo("peanut_butter"));
    }
}
