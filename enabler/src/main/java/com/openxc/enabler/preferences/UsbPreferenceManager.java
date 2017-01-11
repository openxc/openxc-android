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
        return new PreferenceListenerImpl(this);
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

    /**
     * Internal implementation of the {@link VehiclePreferenceManager.PreferenceListener}
     * interface.
     */
    private static final class PreferenceListenerImpl extends PreferenceListener {

        private final static int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.vehicle_interface_key
        };

        /**
         * Main constructor.
         *
         * @param reference Reference to the enclosing class.
         */
        private PreferenceListenerImpl(final VehiclePreferenceManager reference) {
            super(reference);
        }

        @Override
        protected void readStoredPreferences() {
            final UsbPreferenceManager reference = (UsbPreferenceManager) getEnclosingReference();
            if (reference == null) {
                Log.w(TAG, "Can not read stored preferences, enclosing instance is null");
                return;
            }

            reference.setUsbStatus(reference.getPreferences().getString(
                    reference.getString(R.string.vehicle_interface_key), "").equals(
                    reference.getString(R.string.usb_interface_option_value)));
        }

        @Override
        protected int[] getWatchedPreferenceKeyIds() {
            return WATCHED_PREFERENCE_KEY_IDS;
        }
    }
}
