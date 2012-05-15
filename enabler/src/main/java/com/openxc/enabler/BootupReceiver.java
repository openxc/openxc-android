package com.openxc.enabler;

import com.openxc.VehicleManager;

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
public class BootupReceiver extends BroadcastReceiver {
    private final static String TAG = "BootupReceiver";

    // TODO what about when the device is already started? need an app to hit?
    // or do we rely on it being started by the bind call? might get duplicate
    // USB permission requests that way, but maybe it's OK.
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Starting vehicle service on boot");
        context.startService(new Intent(context, VehicleManager.class));
    }
}
