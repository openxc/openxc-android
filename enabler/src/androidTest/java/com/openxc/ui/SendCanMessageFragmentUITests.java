package com.openxc.ui;

import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxcplatform.enabler.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.fragment.app.Fragment;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class SendCanMessageFragmentUITests {

    @Rule
    public FragmentTestRule<?, Fragment> mActivityTestRule = new FragmentTestRule<>(OpenXcEnablerActivity.class,Fragment.class);
    View v;

    @Before
    public void setup(){
        try{
            v = View.inflate(mActivityTestRule.getActivity().getApplicationContext(), R.layout.send_can_message_fragment, null);
        }
        catch(Exception e){
            Log.e("Inflate Exception",Log.getStackTraceString(e));
        }

   }

    @Test
    public void check_for_view_layout_data(){
        assertNotNull(v);
    }
    /*@Test
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

    }*/
}
