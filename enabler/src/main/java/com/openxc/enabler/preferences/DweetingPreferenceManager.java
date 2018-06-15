package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.buglabs.dweetlib.DweetLib;
import com.openxc.sinks.DweetSink;
import com.openxc.sinks.VehicleDataSink;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable sending of vehicle data to dweet.io.
 *
 * The thingname to send data is read from the shared
 * preferences.
 */
public class DweetingPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "DweetPreferenceManager";
    private VehicleDataSink mDweeter;

    public DweetingPreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopDweeting();
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.dweeting_checkbox_key,
                R.string.dweeting_thingname_key
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setDweetingStatus(getPreferences().getBoolean(getString(
                                R.string.dweeting_checkbox_key), false));
            }
        };
    }

    private void setDweetingStatus(boolean enabled) {
        Log.i(TAG, "Setting dweet to " + enabled);
        SharedPreferences.Editor editor = getPreferences().edit();
        String thingname = getPreferenceString(R.string.dweeting_thingname_key);
        if (thingname == null || thingname.equals("")) {
            thingname = DweetLib.getInstance(getContext()).getRandomThingName();
            editor.putString(getString(R.string.dweeting_thingname_key), thingname);
            editor.putString(getString(R.string.dweeting_thingname_default), thingname);
            editor.apply();
        }
        if(enabled) {
            if(mDweeter != null) {
                stopDweeting();
            }

            try {
                mDweeter = new DweetSink(getContext(), thingname);
            } catch(Exception e) {
                Log.w(TAG, "Unable to add dweet sink", e);
                return;
            }
            getVehicleManager().addSink(mDweeter);

        } else {
            stopDweeting();
        }
    }

    private void stopDweeting() {
        if(getVehicleManager() != null){
            Log.d(TAG,"removing Dweet sink");
            getVehicleManager().removeSink(mDweeter);
            mDweeter = null;
        }
    }
}
