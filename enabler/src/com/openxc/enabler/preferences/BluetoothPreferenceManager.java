package com.openxc.enabler.preferences;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;

/**
 * Enable or disable receiving vehicle data from a Bluetooth CAN device.
 */
public class BluetoothPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "BluetoothPreferenceManager";
    public final static String AUTO_DEVICE_SELECTION_ENTRY = "Automatic";

    public BluetoothPreferenceManager(Context context) {
        super(context);
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.bluetooth_checkbox_key,
                R.string.bluetooth_mac_key,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setBluetoothStatus(getPreferences().getBoolean(
                            getString(R.string.bluetooth_checkbox_key), false));
            }
        };
    }

    private synchronized void setBluetoothStatus(boolean enabled) {
        Log.i(TAG, "Setting bluetooth data source to " + enabled);
        if(enabled) {
            String deviceAddress = getPreferenceString(
                    R.string.bluetooth_mac_key);
            if(deviceAddress == null || deviceAddress.equals(
                        AUTO_DEVICE_SELECTION_ENTRY)) {
                deviceAddress = searchForVehicleInterface();
            }

            if(deviceAddress != null) {
                getVehicleManager().addVehicleInterface(
                        BluetoothVehicleInterface.class, deviceAddress);
            } else {
                searchForVehicleInterface();
                Log.d(TAG, "No Bluetooth device MAC set yet (" + deviceAddress +
                        "), not starting source");
            }
        } else {
            getVehicleManager().removeVehicleInterface(
                    BluetoothVehicleInterface.class);
        }
    }

    private String searchForVehicleInterface() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        String deviceAddress = null;
        if(adapter != null && adapter.isEnabled()){
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().startsWith(
                            BluetoothVehicleInterface.DEVICE_NAME_PREFIX)) {
                    Log.d(TAG, "Found paired OpenXC BT VI " + device.getName() +
                            ", will be auto-connected.");
                    deviceAddress = device.getAddress();
                }
            }
        }
        return deviceAddress;
    }
}
