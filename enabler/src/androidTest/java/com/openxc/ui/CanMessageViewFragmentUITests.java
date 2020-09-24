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
import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class CanMessageViewFragmentUITests {

    @Rule
    public ActivityTestRule<OpenXcEnablerActivity> mActivityTestRule = new ActivityTestRule<>(OpenXcEnablerActivity.class);

    @Test
    public void check_for_view_layout_data(){
        View v= LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.can_message_details,null);
        assertNotNull(v);
        View vv= LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.can_message_list_fragment,null);
        assertNotNull(vv);
        View vvv= LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.can_message_list_item,null);
        assertNotNull(vvv);
    }

    @Test
    public void check_elements_presence() {
        View v= LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.can_message_details,null);
        assertNotNull(v.findViewById(R.id.bus));
        assertNotNull(v.findViewById(R.id.timestamp));
        assertNotNull(v.findViewById(R.id.id));
        assertNotNull(v.findViewById(R.id.data));
    }

}
