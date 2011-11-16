package com.openxc.remote.sources;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import android.util.Log;

public class UsbVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "UsbVehicleDataSource";
    private static final String ACTION_USB_PERMISSION =
            "com.ford.openxc.USB_PERMISSION";

    private static URI DEFAULT_USB_DEVICE_URI = null;
    static {
        try {
            DEFAULT_USB_DEVICE_URI = new URI("usb://04d8/0053");
        } catch(URISyntaxException e) { }
    }

    private boolean mRunning;
    private UsbManager mManager;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpoint;
    private PendingIntent mPermissionIntent;
    private final Lock mDeviceConnectionLock;
    private final Condition mDevicePermissionChanged;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized(this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(
                            UsbManager.EXTRA_DEVICE);

                    if (device != null && intent.getBooleanExtra(
                                UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        try {
                            mConnection = setupDevice(mManager, device);
                        } catch(UsbDeviceException e) {
                            Log.w("Couldn't open USB device", e);
                            stop();
                            mDevicePermissionChanged.signal();
                        }
                    } else {
                        Log.d(TAG, "Permission denied for device " + device);
                    }
                }
            }
        }
    };

    public UsbVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback, URI device)
            throws VehicleDataSourceException {
        super(context, callback);
        if(device == null) {
            device = DEFAULT_USB_DEVICE_URI;
            Log.i(TAG, "No USB device specified -- using default " +
                    device);
        }

        mRunning = true;

        mDeviceConnectionLock = new ReentrantLock();
        mDevicePermissionChanged = mDeviceConnectionLock.newCondition();

        mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        try {
            setupDevice(mManager,
                Integer.parseInt(device.getAuthority(), 16),
                Integer.parseInt(device.getPath().substring(1), 16));
        } catch(UsbDeviceException e) {
            throw new VehicleDataSourceException("Couldn't open USB device", e);
        }
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

        mDeviceConnectionLock.lock();
        try {
            mDevicePermissionChanged.await();
        } catch(InterruptedException e) {}

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

    private void setupDevice(UsbManager manager, int vendorId,
            int productId) throws UsbDeviceException {
        UsbDevice device = findDevice(manager, vendorId, productId);
        manager.requestPermission(device, mPermissionIntent);
    }

    private UsbDeviceConnection setupDevice(UsbManager manager,
            UsbDevice device) throws UsbDeviceException {
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
