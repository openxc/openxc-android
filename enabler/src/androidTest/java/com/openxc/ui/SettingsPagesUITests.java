package com.openxc.ui;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.LayoutInflater;
import android.view.View;

import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxcplatform.enabler.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class SettingsPagesUITests {

    @Rule
    public ActivityTestRule<OpenXcEnablerActivity> mActivityTestRule = new ActivityTestRule<>(OpenXcEnablerActivity.class);

    @Test
    public void check_for_SettingsPage_preferences(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivityTestRule.getActivity() != null) {
                    assertNotNull(onView(withId(R.xml.recording_preferences)));
                    assertNotNull(onView(withId(R.xml.data_source_preferences)));
                    assertNotNull(onView(withId(R.xml.output_preferences)));
                    assertNotNull(onView(withId(R.xml.about_preferences)));
                    assertNotNull(onView(withId(R.xml.notification_preferences)));
                }
            }
        }, 1000 );

    }

    @Test
    public void check_elements_presence() {
        assertNotNull(onView(withText("Overwrite Native GPS")));
        assertNotNull(onView(withText("Power Drop")));
        assertNotNull(onView(withText("Network Drop")));
        assertNotNull(onView(withText("USB Drop")));
        assertNotNull(onView(withText("About OpenXC")));
        assertNotNull(onView(withText("Version")));
        assertNotNull(onView(withText("7.1.0")));
        assertNotNull(onView(withText("Tap here to view license info")));
    }
}



