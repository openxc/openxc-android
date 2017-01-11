package com.openxc.enabler.preferences;

import android.content.Context;
import android.util.Log;

import com.openxc.VehicleLocationProvider;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable overwriting native GPS measurements with those from the
 * vehicle.
 *
 * @see VehicleLocationProvider#setOverwritingStatus
 */
public class GpsOverwritePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "GpsOverwritePreferenceManager";
    private VehicleLocationProvider mVehicleLocationProvider;

    public GpsOverwritePreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        if(getVehicleManager() != null){
            mVehicleLocationProvider.stop();
            mVehicleLocationProvider = null;
        }
    }

    protected PreferenceListener createPreferenceListener(){
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.gps_overwrite_checkbox_key,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setNativeGpsOverwriteStatus(getPreferences().getBoolean(
                            getString(R.string.gps_overwrite_checkbox_key), false));
            }
        };
    }

    private void setNativeGpsOverwriteStatus(boolean enabled) {
        Log.i(TAG, "Setting native GPS overwriting to " + enabled);
        if(mVehicleLocationProvider == null) {
            mVehicleLocationProvider = new VehicleLocationProvider(getContext(),
                    getVehicleManager());
        }
        mVehicleLocationProvider.setOverwritingStatus(enabled);
    }
}
