package com.openxc.enabler.preferences;

import android.content.Context;
import android.util.Log;
import com.openxc.remote.VehicleServiceException;
import com.openxcplatform.enabler.R;

public class VehicleInterfacePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "VehicleInterfacePreferenceManager";
    public VehicleInterfacePreferenceManager(Context context) {
        super(context);
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
                R.string.vehicle_interface_key
        };

        /**
         * Main constructor.
         *
         * @param reference Reference to the enclosing class.
         */
        private PreferenceListenerImpl(final VehiclePreferenceManager reference) {
            super(reference);
        }

        @Override
        protected void readStoredPreferences() {
            final VehicleInterfacePreferenceManager reference
                    = (VehicleInterfacePreferenceManager) getEnclosingReference();
            if (reference == null) {
                Log.w(TAG, "Can not read stored preferences, enclosing instance is null");
                return;
            }

            String selectedVi = reference.getPreferences().getString(
                    reference.getString(R.string.vehicle_interface_key), "");
            if(selectedVi.equals(reference.getString(
                    R.string.disabled_interface_option_value))) {
                try {
                    reference.getVehicleManager().setVehicleInterface(null);
                } catch(VehicleServiceException e) {
                }
            }
        }

        @Override
        protected int[] getWatchedPreferenceKeyIds() {
            return WATCHED_PREFERENCE_KEY_IDS;
        }
    }

    /*protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.vehicle_interface_key
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                String selectedVi = getPreferences().getString(
                        getString(R.string.vehicle_interface_key), "");
                if(selectedVi.equals(getString(
                        R.string.disabled_interface_option_value))) {
                    try {
                        getVehicleManager().setVehicleInterface(null);
                    } catch(VehicleServiceException e) {
                    }
                }
            }
        };
    }*/
}
