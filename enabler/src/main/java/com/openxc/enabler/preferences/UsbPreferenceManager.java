package com.openxc.enabler.preferences;

import android.content.Context;
import android.util.Log;

import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.remote.VehicleServiceException;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable receiving vehicle data from a USB vehicle interface.
 */
public class UsbPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "UsbPreferenceManager";

    public UsbPreferenceManager(Context context) {
        super(context);
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.vehicle_interface_key
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setUsbStatus(getPreferences().getString(
                            getString(R.string.vehicle_interface_key), "").equals(
                            getString(R.string.usb_interface_option_value)));
            }
        };
    }

    private synchronized void setUsbStatus(boolean enabled) {
        if(enabled) {
            Log.i(TAG, "Enabling the USB vehicle interface");
            try {
                getVehicleManager().setVehicleInterface(
                        UsbVehicleInterface.class);
            } catch(VehicleServiceException e) {
                Log.e(TAG, "Unable to add USB interface");
            }
        }
    }
}
