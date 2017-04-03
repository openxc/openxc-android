package com.openxc.enabler;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxc.interfaces.bluetooth.BluetoothModemVehicleInterface;
import com.openxc.interfaces.bluetooth.BluetoothV2XVehicleInterface;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;

public class BluetoothReceiver extends BroadcastReceiver {
    private final static String TAG = BluetoothReceiver.class.getSimpleName();

    public String connectedDeviceId =null;

    private final boolean isVehicleInterface(BluetoothDevice device) {
        return device != null && device.getName() != null &&
                device.getName().contains(
                    BluetoothVehicleInterface.DEVICE_NAME_PREFIX);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice bluetoothDevice = (BluetoothDevice)
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if(isVehicleInterface(bluetoothDevice)) {
            connectedDeviceId = bluetoothDevice.getName();
	    if(intent.getAction().compareTo(
                        BluetoothDevice.ACTION_ACL_CONNECTED) == 0){
             if(bluetoothDevice.getName().startsWith(BluetoothModemVehicleInterface.DEVICE_NAME_PREFIX)) {
	        // It's a modem.
	        Log.d(TAG, "A Bluetooth OpenXC Modem connected: " + bluetoothDevice.getName());
	    
	        RegisterDevice.setDevice(bluetoothDevice.getName());
	     } 
	     else if(bluetoothDevice.getName().startsWith(BluetoothV2XVehicleInterface.DEVICE_NAME_PREFIX)){
                Log.d(TAG, "A Bluetooth OpenXC V2X connected: " + bluetoothDevice.getName());
						            		
	        RegisterDevice.setDevice(bluetoothDevice.getName());
	    }
	    else
            {
	    	Log.d(TAG, "A Bluetooth OpenXC VI connected: " + bluetoothDevice.getName());
								            		
	   	RegisterDevice.setDevice(bluetoothDevice.getName());
            }
	    
	    context.startService(new Intent(context, VehicleManager.class));
	    context.startService(new Intent(context,
	                   PreferenceManagerService.class));
	  }
	  else {
	     
             if(bluetoothDevice.getName().startsWith(BluetoothModemVehicleInterface.DEVICE_NAME_PREFIX)) {
	      	     Log.d(TAG, "A Bluetooth OpenXC Modem disconnected: " + bluetoothDevice.getName());
		     RegisterDevice.setDevice(bluetoothDevice.getName());
	    	}
	     else if(bluetoothDevice.getName().startsWith(BluetoothV2XVehicleInterface.DEVICE_NAME_PREFIX)){
	   	     Log.d(TAG, "A Bluetooth OpenXC V2X disconnected: " + bluetoothDevice.getName());
	    	     RegisterDevice.setDevice(bluetoothDevice.getName());
		}
	     else
	       	{
	 	     Log.d(TAG, "A Bluetooth OpenXC VI disconnected: " + bluetoothDevice.getName());
		     RegisterDevice.setDevice(bluetoothDevice.getName());
		}
            }
        }
    }
}
