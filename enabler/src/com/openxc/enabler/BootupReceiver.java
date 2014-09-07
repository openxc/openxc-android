package com.openxc.enabler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openxc.enabler.preferences.PreferenceManagerService;

/**
 * Receive the BOOT_COMPLETED signal and start the VehicleManager.
 *
 * The reason to do this in a central location is to centralize USB permissions
 * management.
 */
public class BootupReceiver extends BroadcastReceiver {
    private final static String TAG = BootupReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Loading configured vehicle services on bootup");
        context.startService(new Intent(context, PreferenceManagerService.class));
    }
}
