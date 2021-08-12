package com.openxc.ui;

import android.test.suitebuilder.annotation.LargeTest;
import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxcplatform.enabler.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class StatusFragmentUITests {

    @Rule
    public ActivityTestRule<OpenXcEnablerActivity> mActivityTestRule = new ActivityTestRule<>(OpenXcEnablerActivity.class);

    @Test
    public void check_elements_presence() {
        onView(withText("SEARCH FOR BLUETOOTH VI")).check(matches(isDisplayed()));
        onView(withText("DISCONNECT")).check(matches(isDisplayed()));
        onView(withText("Active Connections")).check(matches(isDisplayed()));
        onView(withText("Messages Received")).check(matches(isDisplayed()));
        onView(withText("Bytes Per Second")).check(matches(isDisplayed()));
        onView(withText("Messages Per Second")).check(matches(isDisplayed()));
        onView(withText("Average Message Size")).check(matches(isDisplayed()));
        onView(withText("VI Version")).check(matches(isDisplayed()));
        onView(withText("VI Device ID")).check(matches(isDisplayed()));
        onView(withText("VI Platform")).check(matches(isDisplayed()));

        onView(withText("USB Dropped")).check(doesNotExist());
        onView(withText("Bluetooth is disabled, can't search for devices")).check(doesNotExist());
        onView(withText("Unable to enable Bluetooth vehicle interface")).check(doesNotExist());
        onView(withText("Bluetooth needs to be enabled for search.")).check(doesNotExist());
        onView(withText("No Trace File")).check(doesNotExist());
    }

}
