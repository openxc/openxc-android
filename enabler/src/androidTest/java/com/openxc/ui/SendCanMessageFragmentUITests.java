package com.openxc.ui;

import android.os.Build;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxcplatform.enabler.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.fragment.app.Fragment;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;


import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class SendCanMessageFragmentUITests {

    @Rule
    public FragmentTestRule<?, Fragment> mActivityTestRule = new FragmentTestRule<>(OpenXcEnablerActivity.class,Fragment.class);
    private View v;


//    @Before
//    public void setup(){
//        try{
//           // v = View.inflate(mActivityTestRule.getActivity().getApplicationContext(), R.layout.send_can_message_fragment,null);
//           // v= LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.send_can_message_fragment,null);
//        }
//        catch(Exception e){
//            Log.e("Inflate Exception",Log.getStackTraceString(e));
//        }
//
//   }
    @SuppressWarnings("TypeParameterUnusedInFormals")
    private <T extends View> T inflate(int layoutResId) {
        return (T) LayoutInflater.from(mActivityTestRule.getActivity().getApplicationContext()).inflate(layoutResId, null);
    }

    @Test
    public void check_for_view_layout_data(){
        Log.i("Android version", Build.VERSION.RELEASE);
        v=inflate( R.layout.send_can_message_fragment);
        assertNotNull(v);
    }

    @Test
    public void check_elements_presence() {
        v=inflate( R.layout.send_can_message_fragment);
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
