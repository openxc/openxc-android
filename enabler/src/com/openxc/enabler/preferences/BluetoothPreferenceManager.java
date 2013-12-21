package com.openxc.enabler.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;

/**
 * Enable or disable receiving vehicle data from a Bluetooth CAN device.
 */
public class BluetoothPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "BluetoothPreferenceManager";

    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, String> mDiscoveredDevices =
            new HashMap<String, String>();

    public BluetoothPreferenceManager(Context context) {
        super(context);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Log.w(TAG, "This device most likely does not have " +
                "a Bluetooth adapter -- skipping device search");
        } else {
            fillBluetoothDeviceList();
        }
    }

    public Map<String, String> getDiscoveredDevices() {
        return (Map<String, String>) mDiscoveredDevices.clone();
    }

    private void fillBluetoothDeviceList() {
        if(mBluetoothAdapter != null) {
            Log.d(TAG, "Starting paired device search");
            Set<BluetoothDevice> pairedDevices =
                mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Found paired device: " + device);
                mDiscoveredDevices.put(device.getAddress(),
                        device.getName() + " (" + device.getAddress() + ")");
            }
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(mDiscoveryReceiver, filter);

        if(mBluetoothAdapter != null) {
            if(mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            Log.i(TAG, "Starting Bluetooth discovery");
            mBluetoothAdapter.startDiscovery();
        }
    }

    private BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "Found unpaired device: " + device);
                    mDiscoveredDevices.put(device.getAddress(),
                            device.getName() + " (" + device.getAddress() +
                            ")");
                }
            }
        }
    };

    @Override
    public void close() {
        super.close();
        if(mDiscoveryReceiver != null) {
            getContext().unregisterReceiver(mDiscoveryReceiver);
            if(mBluetoothAdapter != null) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
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
                        getString(R.string.bluetooth_mac_automatic_option))) {
                deviceAddress = searchForVehicleInterface();
            }

            if(deviceAddress != null) {
                getVehicleManager().addVehicleInterface(
                        BluetoothVehicleInterface.class, deviceAddress);
            } else {
                Log.d(TAG, "No Bluetooth device MAC set yet and no " +
                        "recognized devices paired, not starting source");
                // TODO need to keep attempting
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
                    break;
                }
            }
        }
        return deviceAddress;
    }
}
