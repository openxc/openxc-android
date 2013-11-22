package com.openxc.enabler;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;

public class BluetoothReceiver extends BroadcastReceiver {
    private final static String TAG = BluetoothReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Recieved intent Event: " + intent.getAction());

        // If a Bluetooth device with the OpenXC device name prefix is
        // connected, start the service if it's not already started
        if(intent.getAction().compareTo(
                    BluetoothDevice.ACTION_ACL_CONNECTED) == 0){
            BluetoothDevice bluetoothDevice = (BluetoothDevice)
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(bluetoothDevice != null
                    && bluetoothDevice.getName() != null
                    && bluetoothDevice.getName().contains(
                        BluetoothVehicleInterface.DEVICE_NAME_PREFIX)){

                Log.i(TAG, "Starting vehicle service on bluetooth connection " +
                        "to " + BluetoothVehicleInterface.DEVICE_NAME_PREFIX +
                        "*.");
                context.startService(new Intent(context, VehicleManager.class));
                context.startService(new Intent(context,
                            PreferenceManagerService.class));
            }
        }
    }
}
