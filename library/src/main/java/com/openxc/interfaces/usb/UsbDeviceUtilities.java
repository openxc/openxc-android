package com.openxc.interfaces.usb;

import java.net.URI;
import java.net.URISyntaxException;

import com.openxc.sources.DataSourceResourceException;

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
    public static URI DEFAULT_USB_DEVICE_URI = null;
    static {
        try {
            DEFAULT_USB_DEVICE_URI = new URI("usb://1bc4/0001");
        } catch(URISyntaxException e) { }
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
            throw new DataSourceResourceException(
                "USB device must be of the format " + DEFAULT_USB_DEVICE_URI +
                " -- the given " + uri + " has a bad vendor ID");
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
        try {
            return Integer.parseInt(uri.getPath().substring(1), 16);
        } catch(NumberFormatException e) {
            throw new DataSourceResourceException(
                "USB device must be of the format " + DEFAULT_USB_DEVICE_URI +
                " -- the given " + uri + " has a bad product ID");
        } catch(StringIndexOutOfBoundsException e) {
            throw new DataSourceResourceException(
                "USB device must be of the format " + DEFAULT_USB_DEVICE_URI +
                " -- the given " + uri + " has a bad product ID");
        }
    }
}
