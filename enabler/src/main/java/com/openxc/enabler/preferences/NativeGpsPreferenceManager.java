package com.openxc.enabler.preferences;

import android.content.Context;

import com.openxcplatform.enabler.R;

/**
 * Enable or disable reading GPS from the native Android stack.
 */
public class NativeGpsPreferenceManager extends VehiclePreferenceManager {
    public NativeGpsPreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        getVehicleManager().setNativeGpsStatus(false);
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.native_gps_checkbox_key,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                getVehicleManager().setNativeGpsStatus(getPreferences().getBoolean(
                            getString(R.string.native_gps_checkbox_key), false));
            }
        };
    }
}
