package com.openxc.remote.sources.usb;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Objects;

import com.openxc.remote.sources.JsonVehicleDataSource;

import com.openxc.remote.sources.usb.UsbDeviceException;

import com.openxc.remote.sources.VehicleDataSourceCallbackInterface;
import com.openxc.remote.sources.VehicleDataSourceException;
import com.openxc.remote.sources.VehicleDataSourceResourceException;

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
    private final URI mDeviceUri;
    private final Lock mDeviceConnectionLock;
    private final Condition mDevicePermissionChanged;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(
                        UsbManager.EXTRA_DEVICE);

                if(intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    mConnection = openDeviceConnection(device);
                } else {
                    Log.i(TAG, "User declined permission for device " +
                            device);
                }
            } else if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            } else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.d(TAG, "Device detached");
                disconnectDevice();
            }
        }
    };

    private UsbDeviceConnection openDeviceConnection(UsbDevice device) {
        UsbDeviceConnection connection = null;
        mDeviceConnectionLock.lock();
        if (device != null) {
            try {
                connection = setupDevice(mManager, device);
                Log.i(TAG, "Connected to USB device with " +
                        connection);
            } catch(UsbDeviceException e) {
                Log.w("Couldn't open USB device", e);
            } finally {
                mDevicePermissionChanged.signal();
                mDeviceConnectionLock.unlock();
            }
        } else {
            Log.d(TAG, "Permission denied for device " + device);
        }
        return connection;
    }

    public UsbVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback, URI device)
            throws VehicleDataSourceException {
        super(context, callback);
        if(device == null) {
            device = DEFAULT_USB_DEVICE_URI;
            Log.i(TAG, "No USB device specified -- using default " +
                    device);
        }

        if(!device.getScheme().equals("usb")) {
            throw new VehicleDataSourceResourceException(
                    "USB device URI must have the usb:// scheme");
        }

        mRunning = true;
        mDeviceUri = device;
        mDeviceConnectionLock = new ReentrantLock();
        mDevicePermissionChanged = mDeviceConnectionLock.newCondition();

        mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        int vendor = vendorFromUri(device);
        int product = productFromUri(device);
        try {
            setupDevice(mManager, vendor, product);
        } catch(VehicleDataSourceException e) {
            Log.i(TAG, "Unable to load USB device -- waiting for it to appear",
                    e);
        }
    }

    public UsbVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback)
            throws VehicleDataSourceException{
        this(context, callback, null);
    }

    private static int vendorFromUri(URI uri)
            throws VehicleDataSourceResourceException {
        try {
            return Integer.parseInt(uri.getAuthority(), 16);
        } catch(NumberFormatException e) {
            throw new VehicleDataSourceResourceException(
                "USB device must be of the format " + DEFAULT_USB_DEVICE_URI +
                " -- the given " + uri + " has a bad vendor ID");
        }
    }

    private static int productFromUri(URI uri)
            throws VehicleDataSourceResourceException {
        try {
            return Integer.parseInt(uri.getPath().substring(1), 16);
        } catch(NumberFormatException e) {
            throw new VehicleDataSourceResourceException(
                "USB device must be of the format " + DEFAULT_USB_DEVICE_URI +
                " -- the given " + uri + " has a bad product ID");
        } catch(StringIndexOutOfBoundsException e) {
            throw new VehicleDataSourceResourceException(
                "USB device must be of the format " + DEFAULT_USB_DEVICE_URI +
                " -- the given " + uri + " has a bad product ID");
        }
    }

    private void disconnectDevice() {
        if(mConnection != null) {
            Log.d(TAG, "Closing connection " + mConnection + " with USB device");
            mDeviceConnectionLock.lock();
            mConnection.close();
            mConnection = null;
            mDeviceConnectionLock.unlock();
        }
    }

    public void stop() {
        Log.d(TAG, "Stopping USB listener");
        mRunning = false;
        mDeviceConnectionLock.lock();
        mDevicePermissionChanged.signal();
        mDeviceConnectionLock.unlock();
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    public void run() {
        waitForDeviceConnection();

        byte[] bytes = new byte[128];
        StringBuffer buffer = new StringBuffer();
        while(mRunning && mConnection != null) {
            waitForDeviceConnection();

            mDeviceConnectionLock.lock();
            if(!mRunning || mConnection == null) {
                break;
            }
            int received = mConnection.bulkTransfer(
                    mEndpoint, bytes, bytes.length, 0);
            if(received > 0) {
                byte[] receivedBytes = new byte[received];
                System.arraycopy(bytes, 0, receivedBytes, 0, received);
                buffer.append(new String(receivedBytes));

                parseStringBuffer(buffer);
            }
            mDeviceConnectionLock.unlock();
        }
    }

    private void waitForDeviceConnection() {
        mDeviceConnectionLock.lock();
        while(mRunning && mConnection == null) {
            Log.d(TAG, "Still no device available");
            try {
                mDevicePermissionChanged.await();
            } catch(InterruptedException e) {}
        }
        mDeviceConnectionLock.unlock();
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
            int productId) throws VehicleDataSourceResourceException {
        UsbDevice device = findDevice(manager, vendorId, productId);
        if(manager.hasPermission(device)) {
            openDeviceConnection(device);
        } else {
            manager.requestPermission(device, mPermissionIntent);
        }
    }

    private UsbDeviceConnection setupDevice(UsbManager manager,
            UsbDevice device) throws UsbDeviceException {
        UsbInterface iface = device.getInterface(0);
        Log.d(TAG, "Connecting to endpoint 1 on interface " + iface);
        mEndpoint = iface.getEndpoint(1);
        return connectToDevice(manager, device, iface);
    }

    private UsbDevice findDevice(UsbManager manager, int vendorId,
            int productId) throws VehicleDataSourceResourceException {
        Log.d(TAG, "Looking for USB device with vendor ID " + vendorId +
                " and product ID " + productId);

        for(UsbDevice candidateDevice : manager.getDeviceList().values()) {
            if(candidateDevice.getVendorId() == vendorId
                    && candidateDevice.getProductId() == productId) {
                Log.d(TAG, "Found USB device " + candidateDevice);
                return candidateDevice;
            }
        }

        throw new VehicleDataSourceResourceException("USB device with vendor " +
                "ID " + vendorId + " and product ID " + productId +
                " not found");
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("device", mDeviceUri)
            .add("connection", mConnection)
            .add("endpoint", mEndpoint)
            .add("callback", getCallback())
            .toString();
    }
}
