package com.openxc.ui;

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
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class DashboardFragmentUITests {

    @Rule
    public ActivityTestRule<OpenXcEnablerActivity> mActivityTestRule = new ActivityTestRule<>(OpenXcEnablerActivity.class);

    @Test
    public void check_for_view_layout_data(){
        View v=LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.can_message_list_fragment,null);
         assertNotNull(v);
    }

    @Test
    public void check_for_toast_data_not_displayed(){
        onView(withText("VI Power Droped")).check(doesNotExist());
    }
}
