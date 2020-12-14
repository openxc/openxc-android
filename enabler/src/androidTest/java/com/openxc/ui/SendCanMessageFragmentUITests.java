package com.openxc.ui;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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

    @Test
    public void check_for_view_layout_data(){
        Log.i("Android version", Build.VERSION.RELEASE);
        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Run your task here
                if (mActivityTestRule.getActivity() != null) {
                    View v = LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.send_can_message_fragment, null);
                    assertNotNull(v);
                }
            }
        }, 1000 );


    }

    @Test
    public void check_elements_presence() {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivityTestRule.getActivity() != null) {
                View v= LayoutInflater.from(mActivityTestRule.getActivity()).inflate(R.layout.send_can_message_fragment,null);
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
        }, 1000 );
    }
}
