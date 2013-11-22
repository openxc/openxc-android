package com.openxc.enabler;

import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.PreferenceManagerService;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receive the BOOT_COMPLETED signal and start the VehicleManager.
 *
 * The reason to do this in a central location is to centralize USB permissions
 * management.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private final static String TAG = BluetoothReceiver.class.getSimpleName();

    // TODO what about when the device is already started? need an app to hit?
    // or do we rely on it being started by the bind call? might get duplicate
    // USB permission requests that way, but maybe it's OK.
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(TAG, "Recieved intent Event: " + intent.getAction());
    	
    	// If "OpenXC-VI-*" is connected via Bluetooth, start service
    	if(intent.getAction().compareTo(BluetoothDevice.ACTION_ACL_CONNECTED) == 0){
    		BluetoothDevice bluetoothDevice = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    		
    		if(bluetoothDevice != null 
    				&& bluetoothDevice.getName() != null
    				&& bluetoothDevice.getName().contains("OpenXC-VI-")){    	
    			
    	        Log.i(TAG, "Starting vehicle service on bluetooth connection to 'OpenXC-VI-*'.");
    	        context.startService(new Intent(context, VehicleManager.class));
    	        context.startService(new Intent(context, PreferenceManagerService.class));
    		}
    	}
    }
}
