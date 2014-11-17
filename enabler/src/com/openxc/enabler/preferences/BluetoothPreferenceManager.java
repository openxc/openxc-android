package com.openxc.enabler.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.interfaces.bluetooth.BluetoothException;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.bluetooth.DeviceManager;
import com.openxc.remote.VehicleServiceException;
import com.openxc.util.SupportSettingsUtils;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable receiving vehicle data from a Bluetooth vehicle interface.
 *
 * When you enable the Bluetooth vehicle interface option, this is what happens:
 *
 *     * The BT discovery process is started to find any nearby VIs that are
 *          powered up - they don't have to be previously paired.
 *     * The Bluetooth device list in preferences is populated with a list of
 *          all paired and discovered devices
 *     * A socket is open and will accept incoming Bluetooth connections, e.g.
 *          a connection initated by a VI acting as Bluetooth master. This
 *          socket remains open as long as the Bluetooth vehicle interface is
 *          enabled and is not currently connected.
 *     * If the "use background polling" option is enabled (the default), we
 *          try and initiate a connection to a discovered or previously selected
 *          Bluetooth device. By default, the device preference is set to automatic
 *          mode. In that mode, the
 *          service will scan for any paired or unpaired Bluetooth device with a
 *          name beginning with "OpenXC-VI-". For each device found, it attempts
 *          to connect and read data. This automatic scan is rather resource
 *          intensive, so it only happens when manuaully initialized with a UI
 *          button *or* once when the service first starts up if no VI has even
 *          been previously connected. If a VI has been previously connected, in
 *          the future it will only poll for a connection to that device.
 *     * You can also explicitly select a device from the list, even one that
 *          doesn't have the OpenXC-VI name prefix. Requesting an automatic scan
 *          will reset this preference back to automatic mode.
 *
 * Device discovery is only kicked off once, when the Bluetooth option is first
 * enabled or the OpenXC service first starts, to avoid draining the battery.
 */
public class BluetoothPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "BluetoothPreferenceManager";

    private DeviceManager mBluetoothDeviceManager;
    private HashMap<String, String> mDiscoveredDevices =
            new HashMap<String, String>();

    public BluetoothPreferenceManager(Context context) {
        super(context);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mDiscoveryReceiver, filter);

        try {
            mBluetoothDeviceManager = new DeviceManager(context);
            fillBluetoothDeviceList();
        } catch(BluetoothException e) {
            Log.w(TAG, "This device most likely does not have " +
                    "a Bluetooth adapter");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getDiscoveredDevices() {
        return (Map<String, String>) mDiscoveredDevices.clone();
    }

    @Override
    public void close() {
        super.close();
        getContext().unregisterReceiver(mDiscoveryReceiver);
        mBluetoothDeviceManager.stop();
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.vehicle_interface_key,
                R.string.bluetooth_polling_key,
                R.string.bluetooth_mac_key,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setBluetoothStatus(getPreferences().getString(
                            getString(R.string.vehicle_interface_key), "").equals(
                            getString(R.string.bluetooth_interface_option_value)));
                getVehicleManager().setBluetoothPollingStatus(
                        getPreferences().getBoolean(
                            getString(R.string.bluetooth_polling_key), true));
            }
        };
    }

    private synchronized void setBluetoothStatus(boolean enabled) {
        if(enabled) {
            Log.i(TAG, "Enabling the Bluetooth vehicle interface");
            String deviceAddress = getPreferenceString(
                    R.string.bluetooth_mac_key);
            if(deviceAddress == null || deviceAddress.equals(
                        getString(R.string.bluetooth_mac_automatic_option))) {
                deviceAddress = null;
                Log.d(TAG, "No Bluetooth vehicle interface selected -- " +
                        "starting in automatic mode");
            }

            try {
                getVehicleManager().setVehicleInterface(
                        BluetoothVehicleInterface.class, deviceAddress);
            } catch(VehicleServiceException e) {
                Log.e(TAG, "Unable to start Bluetooth interface", e);
            }
        }
    }

    private void fillBluetoothDeviceList() {
        for(BluetoothDevice device :
                mBluetoothDeviceManager.getPairedDevices()) {
            mDiscoveredDevices.put(device.getAddress(),
                    device.getName() + " (" + device.getAddress() + ")");
        }

        persistCandidateDiscoveredDevices();
        mBluetoothDeviceManager.startDiscovery();
    }

    private BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String summary = device.getName() + " (" +
                            device.getAddress() + ")";
                    Log.d(TAG, "Found unpaired device: " + summary);
                    mDiscoveredDevices.put(device.getAddress(), summary);
                    persistCandidateDiscoveredDevices();
                }
            }
        }
    };

    private void persistCandidateDiscoveredDevices() {
        // TODO I don't think the MULTI_PROCESS flag is necessary
        SharedPreferences.Editor editor =
                getContext().getSharedPreferences(
                        DeviceManager.KNOWN_BLUETOOTH_DEVICE_PREFERENCES,
                        Context.MODE_MULTI_PROCESS).edit();
        Set<String> candidates = new HashSet<String>();
        for(Map.Entry<String, String> device : mDiscoveredDevices.entrySet()) {
            if(device.getValue().startsWith(
                        BluetoothVehicleInterface.DEVICE_NAME_PREFIX)) {
                candidates.add(device.getKey());
            }
        }
        SupportSettingsUtils.putStringSet(editor,
                DeviceManager.KNOWN_BLUETOOTH_DEVICE_PREF_KEY, candidates);
        editor.commit();
    }
}
