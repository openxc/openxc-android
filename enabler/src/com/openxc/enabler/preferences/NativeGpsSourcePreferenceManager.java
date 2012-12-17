package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.VehicleManager;
import com.openxc.enabler.R;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sources.NativeLocationSource;
import com.openxc.sources.VehicleDataSource;

public class NativeGpsSourcePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "NativeGpsSourcePreferenceManager";

    private VehicleDataSource mNativeLocationSource;

    public NativeGpsSourcePreferenceManager(Context context, VehicleManager vehicle) {
        super(context, vehicle);
    }

    /**
     * Enable or disable reading GPS from the native Android stack.
     *
     * @param enabled true if native GPS should be passed through
     * @throws VehicleServiceException if native GPS status is unable to be set
     *      - an exceptional situation that shouldn't occur.
     */
    public void setNativeGpsStatus(boolean enabled)
            throws VehicleServiceException {
        Log.i(TAG, "Setting native GPS to " + enabled);
        if(enabled) {
            mNativeLocationSource = new NativeLocationSource(getContext());
            getVehicleManager().addSource(mNativeLocationSource);
        } else if(mNativeLocationSource != null) {
            getVehicleManager().removeSource(mNativeLocationSource);
            mNativeLocationSource = null;
        }
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new NativeGpsSourcePreferenceListener(preferences);
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
            try {
                if(key.equals(getString(R.string.native_gps_checkbox_key))) {
                    setNativeGpsStatus(preferences.getBoolean(key, false));
                }
            } catch(VehicleServiceException e) {
                Log.w(TAG, "Unable to update vehicle service when preference \""
                        + key + "\" changed", e);
            }
        }
    }
}
