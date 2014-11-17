package com.openxc.enabler.preferences;

import android.content.Context;

import com.openxc.remote.VehicleServiceException;
import com.openxcplatform.enabler.R;

public class VehicleInterfacePreferenceManager extends VehiclePreferenceManager {
    public VehicleInterfacePreferenceManager(Context context) {
        super(context);
    }

    protected PreferenceListener createPreferenceListener() {
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
    }
}
