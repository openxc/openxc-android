package com.openxc.interfaces.usb;

import android.util.Log;

import com.openxc.sources.DataSourceResourceException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Stateless utilities for finding and opening USB devices.
 *
 * The URI format expected by these functions is:
 *
 *      usb://vendor_id/device_id
 *
 * where both vendor ID and device ID are hex values without a "0x" prefix. An
 * example valid URI is "usb://04d8/0053".
 */
public class UsbDeviceUtilities {
    public static final String USB_DEVICE_ERROR = "USB device must be of the format %s -- the given %s has a bad vendor ID";
    public static URI DEFAULT_USB_DEVICE_URI = null;
    private  UsbDeviceUtilities() {

    }

    static {
        try {
            DEFAULT_USB_DEVICE_URI = new URI("usb://1bc4/0001");
        } catch(URISyntaxException e) {
            Log.e("USB", "exception: " + e.toString());
        }
    }

    /**
     * Return an integer vendor ID from a URI specifying a USB device.
     *
     * @param uri the USB device URI
     * @throws DataSourceResourceException If the URI doesn't match the
     *      format usb://vendor_id/device_id
     */
    public static int vendorFromUri(URI uri)
            throws DataSourceResourceException {
        try {
            return Integer.parseInt(uri.getAuthority(), 16);
        } catch(NumberFormatException e) {
            String msg=String.format(USB_DEVICE_ERROR,DEFAULT_USB_DEVICE_URI,uri);
            throw new DataSourceResourceException(msg);
        }
    }

    /**
     * Return an integer product ID from a URI specifying a USB device.
     *
     * @param uri the USB device URI
     * @throws DataSourceResourceException If the URI doesn't match the
     *      format usb://vendor_id/device_id
     */
    public static int productFromUri(URI uri)
            throws DataSourceResourceException {
        String msg=String.format(USB_DEVICE_ERROR,DEFAULT_USB_DEVICE_URI,uri);
        try {
            return Integer.parseInt(uri.getPath().substring(1), 16);
        } catch(StringIndexOutOfBoundsException|NumberFormatException e) {
            throw new DataSourceResourceException(msg);
        }
    }
}
