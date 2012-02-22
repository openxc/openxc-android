package com.openxc.remote.sources.usb;

import android.app.Activity;

import android.util.Log;

/**
 *
 * The UsbDeviceActivity is a proxy to listen for USB_DEVICE_ATTACHED intents.
 *
 * Android unfortunately doesn't provide a way to specify USB device filters for
 * the ATTACHED intent programatically - it must be in the AndroidManifest. This
 * means we have no way to link the correct device filters with the actual
 * instance of the BroadcastReceiver (an object inside of UsbVehicleDataSource),
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

    // TODO we need to bring back the rebroadcast of an openxc-internal intent
    // for the data souce to pick up, because if the enabler has started the
    // service in the background and no openxc app is in the foreground, this
    // workaround isn't going to work.
    //
    // that seems like an OK solution to me - we should actually be able to
    // specify it in the manifest if we use a broadcastreceiver that's not a
    // nested class.
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "USB device proxy listener woke up");
        finish();
    }
}
