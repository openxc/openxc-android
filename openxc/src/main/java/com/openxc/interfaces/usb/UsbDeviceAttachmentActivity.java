package com.openxc.interfaces.usb;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 *
 * The UsbDeviceActivity is a proxy to listen for USB_DEVICE_ATTACHED intents.
 *
 * Android unfortunately doesn't provide a way to specify USB device filters for
 * the ATTACHED intent programatically - it must be in the AndroidManifest. This
 * means we have no way to link the correct device filters with the actual
 * instance of the BroadcastReceiver (an object inside of UsbVehicleInterface),
 * since we are doing something besides just starting an activity based on the
 * intent.
 *
 * This seems like an oversight by the Android developers - they only envisioned
 * you using the ATTACHED signal to auto-start your application.
 *
 * This Activity is a workaround that listens for the intent. Originally it
 * rebroadcast a custom version that the true listener can actually receive, but
 * that is actually unneccessary - when the activity's blank screen opens and
 * immediately closes (invisble to the user), it causes the active OpenXC app to
 * pause. That currently means the background services all get destroyed and
 * recreated when the activity exits and the previous activity regains focus.
 *
 * After the services restart, the USB devices is reconnected.
 */
public class UsbDeviceAttachmentActivity extends Activity {
    private final static String TAG = "UsbDeviceAttachmentActivity";

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "USB device proxy listener woke up");

        Intent intent = getIntent();
        Log.d(TAG, "Resumed with intent: " + intent);
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            Intent refreshDeviceIntent = new Intent(
                    UsbVehicleInterface.ACTION_USB_DEVICE_ATTACHED);
            sendBroadcast(refreshDeviceIntent);
        }

        finish();
    }
}
