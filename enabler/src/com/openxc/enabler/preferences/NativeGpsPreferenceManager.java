package com.openxc.enabler.preferences;

import android.content.Context;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.sources.NativeLocationSource;
import com.openxc.sources.VehicleDataSource;

/**
 * Enable or disable reading GPS from the native Android stack.
 */
public class NativeGpsPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "NativeGpsPreferenceManager";

    private VehicleDataSource mNativeLocationSource;

    public NativeGpsPreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopNativeGps();
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
                setNativeGpsStatus(getPreferences().getBoolean(
                            getString(R.string.native_gps_checkbox_key), false));
            }
        };
    }

    private void setNativeGpsStatus(boolean enabled) {
        Log.i(TAG, "Setting native GPS to " + enabled);
        if(enabled && mNativeLocationSource == null) {
            mNativeLocationSource = new NativeLocationSource(getContext());
            getVehicleManager().addSource(mNativeLocationSource);
        } else if(!enabled) {
            stopNativeGps();
        }
    }

    private void stopNativeGps() {
        if(getVehicleManager() != null && mNativeLocationSource != null){
            getVehicleManager().removeSource(mNativeLocationSource);
            mNativeLocationSource = null;
        }
    }
}
