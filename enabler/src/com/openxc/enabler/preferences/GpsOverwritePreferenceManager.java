package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sinks.MockedLocationSink;

public class GpsOverwritePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "GpsOverwritePreferenceManager";
    private MockedLocationSink mMockedLocationSink;

    public GpsOverwritePreferenceManager(Context context) {
        super(context);
    }

    /**
     * Enable or disable overwriting native GPS measurements with those from the
     * vehicle.
     *
     * @see MockedLocationSink#setOverwritingStatus
     *
     * @param enabled true if native GPS should be overwritte.
     * @throws VehicleServiceException if GPS overwriting status is unable to be
     *      set - an exceptional situation that shouldn't occur.
     */
    public void setNativeGpsOverwriteStatus(boolean enabled)
            throws VehicleServiceException {
        Log.i(TAG, "Setting native GPS overwriting to " + enabled);
        if(mMockedLocationSink == null) {
            mMockedLocationSink = new MockedLocationSink(getContext());
            getVehicleManager().addSink(mMockedLocationSink);
        }
        mMockedLocationSink.setOverwritingStatus(enabled);
    }

    public void close() {
        super.close();
        getVehicleManager().removeSink(mMockedLocationSink);
        mMockedLocationSink = null;
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new GpsOverwritePreferenceListener(preferences);
    }

    private class GpsOverwritePreferenceListener extends PreferenceListener {

        public GpsOverwritePreferenceListener(SharedPreferences preferences) {
            super(preferences);
        }

        public void readStoredPreferences() {
            onSharedPreferenceChanged(mPreferences,
                        getString(R.string.gps_overwrite_checkbox_key));
        }

        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            try {
                if(key.equals(getString(R.string.gps_overwrite_checkbox_key))) {
                    setNativeGpsOverwriteStatus(preferences.getBoolean(key, false));
                }
            } catch(VehicleServiceException e) {
                Log.w(TAG, "Unable to update vehicle service when preference \""
                        + key + "\" changed", e);
            }
        }
    }
}
