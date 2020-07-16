package com.openxc.ui;

import android.test.suitebuilder.annotation.LargeTest;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.openxc.enabler.DiagnosticRequestFragment;
import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxcplatform.enabler.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
//@RunWith(AndroidJUnit4ClassRunner.class)
@RunWith(AndroidJUnit4.class)
public class DiagnosticUITests {

    //*
    private String stringToBetyped;

    @Rule
    public FragmentTestRule<?, DiagnosticRequestFragment> fragmentTestRule =
            FragmentTestRule.create(DiagnosticRequestFragment.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        stringToBetyped = "Espresso";
    }

    @Test
    public void changeText_sameActivity() {
        // Type text
        onView(withId(R.id.diag_request_id)).perform(typeText(stringToBetyped), closeSoftKeyboard());

        // Validate text
        onView(withId(R.id.diag_request_id)).check(matches(withText(stringToBetyped)));
    }
    //*/
}
