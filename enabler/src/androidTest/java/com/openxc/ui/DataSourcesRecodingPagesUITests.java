package com.openxc.ui;

import android.preference.PreferenceManager;
import android.test.suitebuilder.annotation.LargeTest;

import com.openxc.enabler.OpenXcEnablerActivity;
import com.openxcplatform.enabler.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class DataSourcesRecodingPagesUITests {

    @Rule
    public ActivityTestRule<OpenXcEnablerActivity> mActivityTestRule = new ActivityTestRule<>(OpenXcEnablerActivity.class);

    @Test
    public void check_for_datasources_preference(){
        assertNotNull(onView(withId(R.xml.data_source_preferences)));
       assertTrue(isShown(R.string.vehicle_interface_key));
        assertTrue(isShown(R.string.data_format_key));
        assertTrue(isShown(R.string.native_gps_checkbox_key));
        assertTrue(isShown(R.string.bluetooth_polling_key));
        assertFalse(isShown(R.string.network_host_key));
        assertFalse(isShown(R.string.network_port_key));
        assertFalse(isShown(R.string.trace_source_file_key));
        assertTrue(isShown(R.string.trace_source_playing_checkbox_key));
        assertTrue(isShown(R.string.phone_source_polling_checkbox_key));
    }

    @Test
    public void check_for_recording_preference(){
        assertNotNull(onView(withId(R.xml.recording_preferences)));
        assertTrue(isShown(R.string.recording_directory_key));
        assertTrue(isShown(R.string.uploading_checkbox_key));
        assertTrue(isShown(R.string.uploading_path_key));
        assertTrue(isShown(R.string.uploading_source_name_key));
        assertTrue(isShown(R.string.dweeting_checkbox_key));
        assertTrue(isShown(R.string.dweeting_thingname_key));

    }

    public boolean isShown(int id){
        return PreferenceManager.getDefaultSharedPreferences(mActivityTestRule.getActivity().getApplicationContext()).contains(
                mActivityTestRule.getActivity().getString(id));
    }

}
