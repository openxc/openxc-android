package com.openxc.ui;

import android.test.suitebuilder.annotation.LargeTest;

import com.openxc.VehicleManager;
import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxc.messages.DiagnosticRequest;
import com.openxcplatform.enabler.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class DTCUITests {

    //*
    @Rule
    public ActivityTestRule<OpenXcEnablerActivity> mActivityTestRule = new ActivityTestRule<>(OpenXcEnablerActivity.class);

    @Test
    public void checkButton_sameActivity() {

        // Swipe to DTCRequestFragment
        onView(ViewMatchers.withId(R.id.pager_title_strip)).perform(ViewActions.swipeLeft());
        onView(ViewMatchers.withId(R.id.pager_title_strip)).perform(ViewActions.swipeLeft());
        onView(ViewMatchers.withId(R.id.pager_title_strip)).perform(ViewActions.swipeLeft());
        onView(ViewMatchers.withId(R.id.pager_title_strip)).perform(ViewActions.swipeLeft());

        // Click button
        onView(withId(R.id.dtc_request_button)).perform(ViewActions.click());

        // Verify button disabled and verify not clickable
        onView(withId(R.id.dtc_request_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.dtc_request_button)).check(matches(not(isClickable())));
    }
    //*/
}
