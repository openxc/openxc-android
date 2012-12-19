package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.openxc.enabler.R;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.network.NetworkVehicleDataSource;

public class NetworkSourcePreferenceManager extends VehiclePreferenceManager {
    private NetworkVehicleDataSource mNetworkSource;
    private final static String TAG = "NetworkSourcePreferenceManager";

    public NetworkSourcePreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopNetwork();
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.network_checkbox_key,
                R.string.network_host_key,
                R.string.network_port_key,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;

            }
            public void readStoredPreferences() {
                setNetworkSourceStatus(getPreferences().getBoolean(getString(
                                R.string.network_checkbox_key), false));
            }
        };
    }

    /**
     * Enable or disable receiving vehicle data from a Network device
     *
     * @param enabled
     *            true if network should be enabled
     */
    private void setNetworkSourceStatus(boolean enabled) {
        Log.i(TAG, "Setting network data source to " + enabled);
        if(enabled) {
            String address = getPreferenceString(R.string.network_host_key);
            String port = getPreferenceString(R.string.network_port_key);

            if(!NetworkVehicleDataSource.validate(address, port)) {
                String error = "Network host address (" + address +
                    ") not valid -- not starting network data source";
                Log.w(TAG, error);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = getPreferences().edit();
                editor.putBoolean(getString(R.string.uploading_checkbox_key),
                        false);
                editor.commit();
            } else {
                if(mNetworkSource == null ||
                        !mNetworkSource.sameAddress(address, port)) {
                    stopNetwork();
                    try {
                        mNetworkSource = new NetworkVehicleDataSource(
                                address, port, getContext());
                    } catch (DataSourceException e) {
                        Log.w(TAG, "Unable to add network source", e);
                        return;
                    }

                    getVehicleManager().addSource(mNetworkSource);
                } else {
                    Log.d(TAG, "Network connection to address " + address
                            + " already running");
                }
            }
        } else {
            stopNetwork();
        }
    }

    private void stopNetwork() {
        getVehicleManager().removeSource(mNetworkSource);
        mNetworkSource = null;
    }
}
