package com.openxc.remote.sources.usb;

import android.app.Activity;

import android.content.Intent;

import android.hardware.usb.UsbManager;

import android.util.Log;

public class UsbDeviceAttachmentActivity extends Activity {
    private final static String TAG = "UsbDeviceAttachmentActivity";

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Log.d(TAG, "Resumed with intent: " + intent);
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            Intent refreshDeviceIntent = new Intent(
                    UsbVehicleDataSource.ACTION_USB_DEVICE_ATTACHED);
            sendBroadcast(refreshDeviceIntent);
        }
        finish();
    }
}
