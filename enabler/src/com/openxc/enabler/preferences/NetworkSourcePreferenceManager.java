package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.network.NetworkVehicleDataSource;

public class NetworkSourcePreferenceManager extends VehiclePreferenceManager {
    private NetworkVehicleDataSource mNetworkSource;
    private final static String TAG = "NetworkSourcePreferenceManager";

    public NetworkSourcePreferenceManager(Context context) {
        super(context);
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new NetworkSourcePreferenceListener(preferences);
    }

    /**
     * Enable or disable receiving vehicle data from a Network device
     *
     * @param enabled
     *            true if network should be enabled
     * @throws VehicleServiceException
     *             if the listener is unable to be unregistered with the library
     *             internals - an exceptional situation that shouldn't occur.
     */
    private void setNetworkSourceStatus(boolean enabled)
            throws VehicleServiceException {
        Log.i(TAG, "Setting network data source to " + enabled);
        if(enabled) {
            String address = getPreferenceString(R.string.network_host_key);

            if(address != null) {
                // TODO if the address hasn't changed, don't re-initialize
                stopNetwork();

                try {
                    mNetworkSource = new NetworkVehicleDataSource(address,
                            getContext());
                } catch (DataSourceException e) {
                    Log.w(TAG, "Unable to add Network source", e);
                    return;
                }

                getVehicleManager().addSource(mNetworkSource);
            } else {
                Log.d(TAG, "No network host address set yet (" + address +
                        "), not starting source");
            }
        } else {
            stopNetwork();
        }
    }

    public void close() {
        super.close();
        stopNetwork();
    }

    private void stopNetwork() {
        getVehicleManager().removeSource(mNetworkSource);
        mNetworkSource = null;
    }

    private class NetworkSourcePreferenceListener extends PreferenceListener {

        public NetworkSourcePreferenceListener(SharedPreferences preferences) {
            super(preferences);
        }

        public void readStoredPreferences() {
            onSharedPreferenceChanged(mPreferences,
                        getString(R.string.network_checkbox_key));
        }

        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.network_checkbox_key))
                        || key.equals(getString(R.string.network_host_key))) {
                try {
                    setNetworkSourceStatus(preferences.getBoolean(getString(
                                    R.string.network_checkbox_key), false));
                } catch(VehicleServiceException e) {
                    Log.w(TAG, "Unable to update vehicle service when preference \""
                            + key + "\" changed", e);
                }
            }
        }
    }
}
