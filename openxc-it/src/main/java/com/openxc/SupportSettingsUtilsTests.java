package com.openxc;

import java.util.HashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.openxc.util.SupportSettingsUtils;

public class SupportSettingsUtilsTests extends ServiceTestCase<VehicleManager> {
    SharedPreferences preferences;
    Set<String> value;
    String key = "mykey";

    public SupportSettingsUtilsTests() {
        super(VehicleManager.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        value = new HashSet<String>();
        value.add("abcd");
        value.add("1234");
        value.add("zxy");
    }

    @SmallTest
    public void testStoreRetreiveStringSet() {
        SharedPreferences.Editor editor = preferences.edit();
        SupportSettingsUtils.putStringSet(editor, key, value);
        editor.commit();
        assertEquals(value, SupportSettingsUtils.getStringSet(
                    preferences, key, new HashSet<String>()));
    }

    @SmallTest
    public void testRetreiveInvalidKeyStringSet() {
        assertEquals(new HashSet<String>(), SupportSettingsUtils.getStringSet(
                    preferences, key, new HashSet<String>()));
    }
}

