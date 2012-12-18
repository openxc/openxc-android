package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.bluetooth.BluetoothVehicleDataSource;

public class BluetoothSourcePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "BluetoothSourcePreferenceManager";

    private BluetoothVehicleDataSource mBluetoothSource;

    public BluetoothSourcePreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopBluetooth();
    }

    /**
     * Enable or disable receiving vehicle data from a Bluetooth CAN device.
     *
     * @param enabled true if bluetooth should be enabled
     */
    private synchronized void setBluetoothSourceStatus(boolean enabled) {
        Log.i(TAG, "Setting bluetooth data source to " + enabled);
        if(enabled) {
            String deviceAddress = getPreferenceString(
                    R.string.bluetooth_mac_key);
            if(deviceAddress != null ) {
                if(mBluetoothSource == null ||
                        !mBluetoothSource.getAddress().equals(deviceAddress)) {
                    stopBluetooth();

                    try {
                        mBluetoothSource = new BluetoothVehicleDataSource(
                                getContext(), deviceAddress);
                    } catch(DataSourceException e) {
                        Log.w(TAG, "Unable to add Bluetooth source", e);
                        return;
                    }
                    getVehicleManager().addSource(mBluetoothSource);
                } else {
                    Log.d(TAG, "Bluetooth connection to address " + deviceAddress
                            + " already running");
                }
            } else {
                Log.d(TAG, "No Bluetooth device MAC set yet (" + deviceAddress +
                        "), not starting source");
            }
        } else {
            stopBluetooth();
        }
    }

    private synchronized void stopBluetooth() {
        getVehicleManager().removeSource(mBluetoothSource);
        if(mBluetoothSource != null) {
            mBluetoothSource.close();
            mBluetoothSource = null;
        }
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new BluetoothSourcePreferenceListener(preferences);
    }

    private class BluetoothSourcePreferenceListener extends PreferenceListener {

        public BluetoothSourcePreferenceListener(
                SharedPreferences preferences) {
            super(preferences);
        }

        public void readStoredPreferences() {
            onSharedPreferenceChanged(mPreferences,
                        getString(R.string.bluetooth_checkbox_key));
        }

        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.bluetooth_checkbox_key))
                    || key.equals(getString(R.string.bluetooth_mac_key))) {
                setBluetoothSourceStatus(preferences.getBoolean(
                        getString(R.string.bluetooth_checkbox_key), false));
            }
        }
    }
}
