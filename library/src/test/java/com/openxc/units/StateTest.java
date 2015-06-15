package com.openxc.units;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

public class StateTest {
    State<TestState> state;

    private enum TestState {
        ON,
        OFF,
        PEANUT_BUTTER;
    }

    @Before
    public void setUp() {
        state = new State<TestState>(TestState.PEANUT_BUTTER);
    }

    @Test
    public void testSeralizedValue() {
        assertThat(state.getSerializedValue(), equalTo("peanut_butter"));
    }
}
