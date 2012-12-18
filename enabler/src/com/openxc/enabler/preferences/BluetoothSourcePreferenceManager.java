package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.VehicleManager;
import com.openxc.enabler.R;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.bluetooth.BluetoothVehicleDataSource;

public class BluetoothSourcePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "BluetoothSourcePreferenceManager";

    private BluetoothVehicleDataSource mBluetoothSource;

    public BluetoothSourcePreferenceManager(Context context,
            VehicleManager vehicle) {
        super(context, vehicle);
        Log.d(TAG, "Created new " + TAG);
    }

    public void close() {
        super.close();
        stopBluetooth();
    }

    /**
     * Enable or disable receiving vehicle data from a Bluetooth CAN device.
     *
     * @param enabled true if bluetooth should be enabled
     * @throws VehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional
     *      situation that shouldn't occur.
     */
    public synchronized void setBluetoothSourceStatus(boolean enabled)
            throws VehicleServiceException {
        Log.i(TAG, "Setting bluetooth data source to " + enabled);
        if(enabled) {
            String deviceAddress = getPreferenceString(
                    R.string.bluetooth_mac_key);
            if(deviceAddress != null) {
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
            try {
                if(key.equals(getString(R.string.bluetooth_checkbox_key))
                        || key.equals(getString(R.string.bluetooth_mac_key))) {
                    setBluetoothSourceStatus(preferences.getBoolean(
                            getString(R.string.bluetooth_checkbox_key), false));
                }
            } catch(VehicleServiceException e) {
                Log.w(TAG, "Unable to update vehicle service when preference \""
                        + key + "\" changed", e);
            }
        }
    }
}
