package com.openxc.enabler.preferences;

import java.net.InetSocketAddress;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.ethernet.EthernetVehicleDataSource;

public class EthernetPreferenceManager extends VehiclePreferenceManager {
    private EthernetVehicleDataSource mEthernetSource;
    private final static String TAG = "EthernetPreferenceManager";

    public EthernetPreferenceManager(Context context) {
        super(context);
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new EthernetPreferenceListener(preferences);
    }

    /**
     * Enable or disable receiving vehicle data from a Ethernet device
     *
     * @param enabled
     *            true if ethernet should be enabled
     * @throws VehicleServiceException
     *             if the listener is unable to be unregistered with the library
     *             internals - an exceptional situation that shouldn't occur.
     */
    private void setEthernetSourceStatus(boolean enabled)
            throws VehicleServiceException {
        Log.i(TAG, "Setting ethernet data source to " + enabled);
        // TODO if the address hasn't changed, don't re-initialize
        if(enabled) {
            String deviceAddress = getPreferenceString(
                    R.string.ethernet_connection_key);

            InetSocketAddress ethernetAddr;
            String addressSplit[] = deviceAddress.split(":");
            if(addressSplit.length != 2) {
                throw new VehicleServiceException(
                    "Device address in wrong format! Expected: ip:port");
            } else {
                Integer port = new Integer(addressSplit[1]);

                String host = addressSplit[0];
                ethernetAddr = new InetSocketAddress(host, port.intValue());
            }

            if(deviceAddress != null) {
                getVehicleManager().removeSource(mEthernetSource);
                if(mEthernetSource != null) {
                    mEthernetSource.stop();
                }

                try {
                    mEthernetSource = new EthernetVehicleDataSource(
                            ethernetAddr, getContext());
                    mEthernetSource.start();
                } catch (DataSourceException e) {
                    Log.w(TAG, "Unable to add Ethernet source", e);
                    return;
                }
                getVehicleManager().addSource(mEthernetSource);
            } else {
                Log.d(TAG, "No ethernet address set yet (" + deviceAddress +
                        "), not starting source");
            }
        }
        else {
            getVehicleManager().removeSource(mEthernetSource);
            if(mEthernetSource != null) {
                mEthernetSource.stop();
            }
        }
    }

    public void close() {
        super.close();
        stopEthernet();
    }

    private void stopEthernet() {
        getVehicleManager().removeSource(mEthernetSource);
        mEthernetSource = null;
    }

    private class EthernetPreferenceListener extends PreferenceListener {

        public EthernetPreferenceListener(SharedPreferences preferences) {
            super(preferences);
        }

        public void readStoredPreferences() {
            onSharedPreferenceChanged(mPreferences,
                        getString(R.string.ethernet_checkbox_key));
        }

        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.ethernet_checkbox_key))
                        || key.equals(getString(R.string.ethernet_connection_key))) {
                try {
                    setEthernetSourceStatus(preferences.getBoolean(getString(
                                    R.string.ethernet_checkbox_key), false));
                } catch(VehicleServiceException e) {
                    Log.w(TAG, "Unable to update vehicle service when preference \""
                            + key + "\" changed", e);
                }
            }
        }
    }
}
