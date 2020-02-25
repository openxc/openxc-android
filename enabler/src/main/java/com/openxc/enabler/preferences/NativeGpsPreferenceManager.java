package com.openxc.enabler.preferences;

import android.content.Context;
import android.util.Log;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable reading GPS from the native Android stack.
 */
public class NativeGpsPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "VehiclePreferenceManager";
    public NativeGpsPreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        if(getVehicleManager()!=null) {
            getVehicleManager().setNativeGpsStatus(false);
        }
    }
    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListenerImpl(this);
    }
    /**
     * Internal implementation of the {@link VehiclePreferenceManager.PreferenceListener}
     * interface.
     */
    private static final class PreferenceListenerImpl extends PreferenceListener {

        private final static int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.native_gps_checkbox_key
        };

        /**
         * Main constructor.
         *
         * @param reference Reference to the enclosing class.
         */
        private PreferenceListenerImpl(final VehiclePreferenceManager reference) {
            //super(reference);
        }

        @Override
        protected void readStoredPreferences() {
            final NativeGpsPreferenceManager reference
                    = (NativeGpsPreferenceManager) getEnclosingReference();
            if (reference == null) {
                Log.w(TAG, "Can not read stored preferences, enclosing instance is null");
                return;
            }

            reference.getVehicleManager().setNativeGpsStatus(reference.getPreferences().getBoolean(
                    reference.getString(R.string.native_gps_checkbox_key), false));
        }

        @Override
        protected int[] getWatchedPreferenceKeyIds() {
            return WATCHED_PREFERENCE_KEY_IDS;
        }
    }

   /* protected PreferenceListener createPreferenceListener() {
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
    }*/
}
