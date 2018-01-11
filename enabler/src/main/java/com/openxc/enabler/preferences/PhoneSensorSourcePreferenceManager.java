package com.openxc.enabler.preferences;

import android.content.Context;
import android.util.Log;

import com.openxc.remote.VehicleServiceException;
import com.openxc.sources.PhoneSensorSource;
import com.openxcplatform.enabler.R;

/**
 * Created by vish on 5/28/16.
 */

public class PhoneSensorSourcePreferenceManager extends VehiclePreferenceManager{
    private final static String TAG = "PhoneSensorSourcePreferenceManager";

    public PhoneSensorSource mPhoneSensorSource;

    public PhoneSensorSourcePreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopSensorCapture();
    }

    @Override
    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                    R.string.phone_source_polling_checkbox_key
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setPhoneSensorSourceStatus(getPreferences().getBoolean(getString(R.string.phone_source_polling_checkbox_key),false));
            }
        };
    }

    private synchronized void setPhoneSensorSourceStatus(boolean enabled) {
        Log.i(TAG, "Setting phone source setting to " + enabled);
        if(enabled) {
            if(mPhoneSensorSource == null) {
                stopSensorCapture();

                try {
                    mPhoneSensorSource = new PhoneSensorSource(
                            getContext());
                } catch(Exception e) {
                    Log.w(TAG, "Unable to start Phone Sensor Source", e);
                    return;
                }
                getVehicleManager().addSource(mPhoneSensorSource);
            } else {
                Log.d(TAG, "Phone Sensor already activated");
            }
        }
        else {
            stopSensorCapture();
        }
    }

    private synchronized void stopSensorCapture() {
        if(getVehicleManager() != null && mPhoneSensorSource != null){
            getVehicleManager().removeSource(mPhoneSensorSource);
            mPhoneSensorSource = null;
        }
    }
}
