package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.sources.NativeLocationSource;
import com.openxc.sources.VehicleDataSource;

public class NativeGpsSourcePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "NativeGpsSourcePreferenceManager";

    private VehicleDataSource mNativeLocationSource;

    public NativeGpsSourcePreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopNativeGps();
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new NativeGpsSourcePreferenceListener(preferences);
    }

    /**
     * Enable or disable reading GPS from the native Android stack.
     *
     * @param enabled true if native GPS should be passed through
     */
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
        getVehicleManager().removeSource(mNativeLocationSource);
        mNativeLocationSource = null;
    }

    private class NativeGpsSourcePreferenceListener extends PreferenceListener {

        public NativeGpsSourcePreferenceListener(SharedPreferences preferences) {
            super(preferences);
        }

        public void readStoredPreferences() {
            onSharedPreferenceChanged(mPreferences,
                        getString(R.string.native_gps_checkbox_key));
        }

        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.native_gps_checkbox_key))) {
                setNativeGpsStatus(preferences.getBoolean(key, false));
            }
        }
    }
}
