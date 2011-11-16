package com.openxc.remote.sources;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import android.util.Log;

public class UsbVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "UsbVehicleDataSource";
    private static URI DEFAULT_USB_DEVICE_URI = null;
    static {
        try {
            DEFAULT_USB_DEVICE_URI = new URI("usb://04d8/0053");
        } catch(URISyntaxException e) { }
    }

    private boolean mRunning;

    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpoint;

    public UsbVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback, URI device)
            throws VehicleDataSourceException {
        super(callback);
        if(device == null) {
            device = DEFAULT_USB_DEVICE_URI;
            Log.i(TAG, "No USB device specified -- using default " +
                    device);
        }

        mRunning = true;

        try {
            mConnection = setupDevice(
                    (UsbManager) context.getSystemService(Context.USB_SERVICE),
                    Integer.parseInt(device.getAuthority(), 16),
                    Integer.parseInt(device.getPath().substring(1), 16));
        } catch(UsbDeviceException e) {
            throw new VehicleDataSourceException("Couldn't open USB device", e);
        }

        Log.d(TAG, "Starting new USB data source with connection " +
                mConnection);
    }

    public UsbVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback)
            throws VehicleDataSourceException{
        this(context, callback, null);
    }

    public void stop() {
        Log.d(TAG, "Stopping USB listener");
        mRunning = false;
    }

    public void run() {
        byte[] bytes = new byte[128];

        StringBuffer buffer = new StringBuffer();
        while(mRunning && mConnection != null) {
            int received = mConnection.bulkTransfer(
                    mEndpoint, bytes, bytes.length, 0);
            byte[] receivedBytes = new byte[received];
            System.arraycopy(bytes, 0, receivedBytes, 0, received);
            buffer.append(new String(receivedBytes));

            parseStringBuffer(buffer);
        }
    }

    private void parseStringBuffer(StringBuffer buffer) {
        int newlineIndex = buffer.indexOf("\r\n");
        if(newlineIndex != -1) {
            final String messageString = buffer.substring(0, newlineIndex);
            buffer.delete(0, newlineIndex + 1);
            handleJson(messageString);
        }
    }

    private UsbDeviceConnection setupDevice(UsbManager manager, int vendorId,
            int productId) throws UsbDeviceException {
        UsbDevice device = findDevice(manager, vendorId, productId);
        UsbInterface iface = device.getInterface(0);
        Log.d(TAG, "Connecting to endpoint 1 on interface " + iface);
        mEndpoint = iface.getEndpoint(1);
        return connectToDevice(manager, device, iface);
    }

    private UsbDevice findDevice(UsbManager manager, int vendorId,
            int productId) throws UsbDeviceException {
        Log.d(TAG, "Looking for USB device with vendor ID " + vendorId +
                " and product ID " + productId);
        for(UsbDevice candidateDevice : manager.getDeviceList().values()) {
            if(candidateDevice.getVendorId() == vendorId
                    && candidateDevice.getProductId() == productId) {
                return candidateDevice;
            }
        }
        throw new UsbDeviceException("USB device with vendor ID " + vendorId +
                    " and product ID " + productId + " not found");
    }

    private UsbDeviceConnection connectToDevice(UsbManager manager,
            UsbDevice device, UsbInterface iface)
            throws UsbDeviceException {
        UsbDeviceConnection connection = manager.openDevice(device);
        if(connection == null) {
            throw new UsbDeviceException("Couldn't open a connection to " +
                    "device -- user may not have given permission");
        }
        connection.claimInterface(iface, true);
        return connection;
    }
}
