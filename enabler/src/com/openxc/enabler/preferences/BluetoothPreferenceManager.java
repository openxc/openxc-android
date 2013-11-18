package com.openxc.enabler.preferences;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;

/**
 * Enable or disable receiving vehicle data from a Bluetooth CAN device.
 */
public class BluetoothPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "BluetoothPreferenceManager";
    
    private final static String OPENXC_VI_PREFIX = "OpenXC-VI-";

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
            if(deviceAddress != null ) {
                getVehicleManager().addVehicleInterface(
                        BluetoothVehicleInterface.class, deviceAddress);
            } else {
            	// Search paired BT devices for OpenXC VI
            	
            	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            	if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
            		// Get paired list of adapaters
            		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            		// If there are paired devices
            		if(pairedDevices.size() > 0){
            			// Loop through paired devices
            			for(BluetoothDevice device:pairedDevices){
            				if(device.getName().startsWith(OPENXC_VI_PREFIX)){
            					// Autopair with this device
            					getVehicleManager().addVehicleInterface(BluetoothVehicleInterface.class, 
            							device.getAddress());
            					
            					SharedPreferences.Editor editor = getPreferences().edit();
            					editor.putString(getContext().getString(R.string.bluetooth_mac_key), 
            							device.getAddress());
            					editor.commit();
            					
            					Log.d(TAG, "Paired Bluetooth device, " + device.getName() + 
            							", will be auto connected.");
            					
            					break;
            				}
            			}
            		}
            	}
            	
                Log.d(TAG, "No Bluetooth device MAC set yet (" + deviceAddress +
                        "), not starting source");
            }
        } else {
            getVehicleManager().removeVehicleInterface(
                    BluetoothVehicleInterface.class);
        }
    }
}
