package com.openxc.ui;

import android.test.suitebuilder.annotation.LargeTest;
import android.view.LayoutInflater;
import android.view.View;

import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxcplatform.enabler.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class SendCanMessageFragmentUITests {

    @Rule
    public ActivityTestRule<OpenXcEnablerActivity> mActivityTestRule = new ActivityTestRule<>(OpenXcEnablerActivity.class);

    View v;

    @Before
    public void setup(){
        v= LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.send_can_message_fragment,null);
    }

    @Test
    public void check_for_view_layout_data(){
        assertNotNull(v);
    }

    @Test
    public void check_elements_presence() {
        assertNotNull(v.findViewById(R.id.message_id));
        assertNotNull(v.findViewById(R.id.message_payload));
        assertNotNull(v.findViewById(R.id.message_payload2));
        assertNotNull(v.findViewById(R.id.message_payload3));
        assertNotNull(v.findViewById(R.id.message_payload4));
        assertNotNull(v.findViewById(R.id.message_payload5));
        assertNotNull(v.findViewById(R.id.message_payload6));
        assertNotNull(v.findViewById(R.id.message_payload7));
        assertNotNull(v.findViewById(R.id.message_payload8));
        assertNotNull(v.findViewById(R.id.bus_spinner));
        v.findViewById(R.id.send_request).performClick();

    }
}
