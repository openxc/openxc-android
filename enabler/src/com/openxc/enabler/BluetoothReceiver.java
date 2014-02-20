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
            if(intent.getAction().compareTo(
                        BluetoothDevice.ACTION_ACL_CONNECTED) == 0){
                Log.d(TAG, "A Bluetooth OpenXC VI connected: " + bluetoothDevice.getName());
                context.startService(new Intent(context, VehicleManager.class));
                context.startService(new Intent(context,
                            PreferenceManagerService.class));
            } else {
                Log.d(TAG, "A Bluetooth OpenXC VI disconnected: " + bluetoothDevice.getName());
            }
        }
    }
}
