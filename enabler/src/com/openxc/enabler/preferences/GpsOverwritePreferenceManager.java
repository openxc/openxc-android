package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
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
     */
    public void setNativeGpsOverwriteStatus(boolean enabled) {
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
            if(key.equals(getString(R.string.gps_overwrite_checkbox_key))) {
                setNativeGpsOverwriteStatus(preferences.getBoolean(key, false));
            }
        }
    }
}
