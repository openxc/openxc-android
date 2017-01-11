package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.interfaces.network.NetworkVehicleInterface;
import com.openxc.remote.VehicleServiceException;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable receiving vehicle data from a Network device
 */
public class NetworkPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "NetworkPreferenceManager";

    public NetworkPreferenceManager(Context context) {
        super(context);
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListenerImpl(this);
    }

    private void setNetworkStatus(boolean enabled) {
        Log.i(TAG, "Setting network data source to " + enabled);
        if(enabled) {
            String address = getPreferenceString(R.string.network_host_key);
            String port = getPreferenceString(R.string.network_port_key);
            String combinedAddress = address + ":" + port;

            if(address == null || port == null ||
                    !NetworkVehicleInterface.validateResource(
                        combinedAddress)) {
                String error = "Network host URI (" + combinedAddress +
                    ") not valid -- not starting network data source";
                Log.w(TAG, error);
                SharedPreferences.Editor editor = getPreferences().edit();
                editor.putBoolean(getString(R.string.uploading_checkbox_key),
                        false);
                editor.commit();
            } else {
                try {
                    getVehicleManager().setVehicleInterface(
                            NetworkVehicleInterface.class, combinedAddress);
                } catch(VehicleServiceException e) {
                    Log.e(TAG, "Unable to add network interface", e);
                }
            }
        }
    }

    /**
     * Internal implementation of the {@link VehiclePreferenceManager.PreferenceListener}
     * interface.
     */
    private static final class PreferenceListenerImpl extends PreferenceListener {

        private final static int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.vehicle_interface_key,
                R.string.network_host_key,
                R.string.network_port_key
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
            final NetworkPreferenceManager reference = (NetworkPreferenceManager) getEnclosingReference();
            if (reference == null) {
                Log.w(TAG, "Can not read stored preferences, enclosing instance is null");
                return;
            }

            reference.setNetworkStatus(reference.getPreferences().getString(
                    reference.getString(R.string.vehicle_interface_key), "").equals(
                    reference.getString(R.string.network_interface_option_value)));
        }

        @Override
        protected int[] getWatchedPreferenceKeyIds() {
            return WATCHED_PREFERENCE_KEY_IDS;
        }
    }
}
