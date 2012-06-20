package com.openxc.sources.usb;

import java.net.URI;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;

import com.openxc.sources.ContextualVehicleDataSource;
import com.openxc.sources.SourceCallback;

import com.openxc.sources.usb.UsbDeviceException;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

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

/**
 * A vehicle data source reading measurements from an OpenXC USB device.
 *
 * This class looks for a USB device and expects to read OpenXC-compatible,
 * newline separated JSON messages in USB bulk transfer packets.
 *
 * The device used (if different from the default) can be specified by passing
 * an custom URI to the constructor. The expected format of this URI is defined
 * in {@link UsbDeviceUtilities}.
 *
 * According to Android's USB device usage requirements, this class requests
 * permission for the USB device from the user before accessing it. This may
 * cause a pop-up dialog that the user must dismiss before the data source will
 * become active.
 */
public class UsbVehicleDataSource extends ContextualVehicleDataSource
        implements Runnable {
    private static final String TAG = "UsbVehicleDataSource";
    public static final String ACTION_USB_PERMISSION =
            "com.ford.openxc.USB_PERMISSION";
    public static final String ACTION_USB_DEVICE_ATTACHED =
            "com.ford.openxc.USB_DEVICE_ATTACHED";

    private boolean mRunning;
    private UsbManager mManager;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpoint;
    private PendingIntent mPermissionIntent;
    private final URI mDeviceUri;
    private final Lock mDeviceConnectionLock;
    private final Condition mDevicePermissionChanged;
    private int mVendorId;
    private int mProductId;
    private double mBytesReceived;

    /**
     * Construct an instance of UsbVehicleDataSource with a receiver callback
     * and custom device URI.
     *
     * If the device cannot be found at initialization, the object will block
     * waiting for a signal to check again.
     *
     * TODO Do we ever send such a signal? Or did I delete that because in order
     * to have that signal sent, we have to shut down the VehicleService
     * (and thus this UsbVehicleDataSource) anyway?
     *
     * @param context The Activity or Service context, used to get access to the
     *      Android UsbManager.
     * @param callback An object implementing the
     *      SourceCallback that should receive data as it is
     *      received and parsed.
     * @param device a USB device URI (see {@link UsbDeviceUtilities} for the
     *      format) to look for.
     * @throws DataSourceException  If the URI doesn't have the correct
     *          format
     */
    public UsbVehicleDataSource(SourceCallback callback, Context context,
            URI device) throws DataSourceException {
        super(callback, context);
        if(device == null) {
            device = UsbDeviceUtilities.DEFAULT_USB_DEVICE_URI;
            Log.i(TAG, "No USB device specified -- using default " +
                    device);
        }

        if(device.getScheme() == null || !device.getScheme().equals("usb")) {
            throw new DataSourceResourceException(
                    "USB device URI must have the usb:// scheme");
        }

        mRunning = true;
        mDeviceUri = device;
        mDeviceConnectionLock = new ReentrantLock();
        mDevicePermissionChanged = mDeviceConnectionLock.newCondition();

        mManager = (UsbManager) getContext().getSystemService(
                Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        mVendorId = UsbDeviceUtilities.vendorFromUri(device);
        mProductId = UsbDeviceUtilities.productFromUri(device);
        try {
            setupDevice(mManager, mVendorId, mProductId);
        } catch(DataSourceException e) {
            Log.i(TAG, "Unable to load USB device -- " +
                    "waiting for it to appear", e);
        }
        new Thread(this).start();
    }

    /**
     * Construct an instance of UsbVehicleDataSource with a receiver callback
     * and the default device URI.
     *
     * The default device URI is specified in {@link UsbDeviceUtilities}.
     *
     * @param context The Activity or Service context, used to get access to the
     *      Android UsbManager.
     * @param callback An object implementing the
     *      SourceCallback that should receive data as it is
     *      received and parsed.
     * @throws DataSourceException  in exceptional circumstances, i.e.
     *      only if the default device URI is malformed.
     */
    public UsbVehicleDataSource(SourceCallback callback, Context context)
            throws DataSourceException {
        this(callback, context, null);
    }

    public UsbVehicleDataSource(Context context)
            throws DataSourceException {
        this(null, context);
    }

    /**
     * Unregister USB device intent broadcast receivers and stop waiting for a
     * connection.
     *
     * This should be called before the object is given up to the garbage
     * collector to aviod leaking a receiver in the Android framework.
     */
    public void stop() {
        super.stop();
        Log.d(TAG, "Stopping USB listener");
        if(!mRunning) {
            Log.d(TAG, "Already stopped.");
            return;
        }
        mRunning = false;
        mDeviceConnectionLock.lock();
        mDevicePermissionChanged.signal();
        mDeviceConnectionLock.unlock();
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Continuously read JSON messages from an attached USB device, or wait for
     * a connection.
     *
     * This loop will only exit if {@link #stop()} is called - otherwise it
     * either waits for a new device connection or reads USB packets.
     */
    public void run() {
        waitForDeviceConnection();

        double lastLoggedTransferStatsAtByte = 0;
        byte[] bytes = new byte[128];
        StringBuffer buffer = new StringBuffer();
        final long startTime = System.nanoTime();
        long endTime;
        while(mRunning) {
            waitForDeviceConnection();

            mDeviceConnectionLock.lock();
            if(mConnection == null) {
            	mDeviceConnectionLock.unlock();
                continue;
            }
            // TODO when there haven't been any USB transfers for a long time,
            // we can get stuck here. do we need a timeout so it retries after
            // USB wakes backup? Why does USB seem to go to sleep in the first
            // place?
            int received = mConnection.bulkTransfer(
                    mEndpoint, bytes, bytes.length, 0);
            if(received > 0) {
                // Creating a new String object for each message causes the
                // GC to go a little crazy, but I don't see another obvious way
                // of converting the byte[] to something the StringBuffer can
                // accept (either char[] or String). See #151.
                buffer.append(new String(bytes, 0, received));

                parseStringBuffer(buffer);
                mBytesReceived += received;
            }
            mDeviceConnectionLock.unlock();

            endTime = System.nanoTime();
            // log the transfer stats roughly every 1MB
            if(mBytesReceived > lastLoggedTransferStatsAtByte + 1024 * 1024) {
                lastLoggedTransferStatsAtByte = mBytesReceived;
                logTransferStats(startTime, endTime);
            }
        }
        Log.d(TAG, "Stopped USB listener");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("device", mDeviceUri)
            .add("connection", mConnection)
            .add("endpoint", mEndpoint)
            .toString();
    }

    private void logTransferStats(final long startTime, final long endTime) {
        double kilobytesTransferred = mBytesReceived / 1000.0;
        long elapsedTime = TimeUnit.SECONDS.convert(
            System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        Log.i(TAG, "Transferred " + kilobytesTransferred + " KB in "
            + elapsedTime + " seconds at an average of " +
            kilobytesTransferred / elapsedTime + " KB/s");
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
        int newlineIndex = buffer.indexOf("\n");
        if(newlineIndex != -1) {
            final String messageString = buffer.substring(0, newlineIndex);
            buffer.delete(0, newlineIndex + 1);
            handleMessage(messageString);
        }
    }

    private void setupDevice(UsbManager manager, int vendorId,
            int productId) throws DataSourceResourceException {
        UsbDevice device = findDevice(manager, vendorId, productId);
        if(manager.hasPermission(device)) {
            Log.d(TAG, "Already have permission to use " + device);
            openDeviceConnection(device);
        } else {
            Log.d(TAG, "Requesting permission for " + device);
            manager.requestPermission(device, mPermissionIntent);
        }
    }

    private UsbDeviceConnection setupDevice(UsbManager manager,
            UsbDevice device) throws UsbDeviceException {
        if(device.getInterfaceCount() != 1) {
            throw new UsbDeviceException("USB device didn't have an " +
                    "interface for us to open");
        }
        UsbInterface iface = device.getInterface(0);
        Log.d(TAG, "Connecting to endpoint 1 on interface " + iface);
        mEndpoint = iface.getEndpoint(1);
        return connectToDevice(manager, device, iface);
    }

    private UsbDevice findDevice(UsbManager manager, int vendorId,
            int productId) throws DataSourceResourceException {
        Log.d(TAG, "Looking for USB device with vendor ID " + vendorId +
                " and product ID " + productId);

        for(UsbDevice candidateDevice : manager.getDeviceList().values()) {
            if(candidateDevice.getVendorId() == vendorId
                    && candidateDevice.getProductId() == productId) {
                Log.d(TAG, "Found USB device " + candidateDevice);
                return candidateDevice;
            }
        }

        throw new DataSourceResourceException("USB device with vendor " +
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

    private void openDeviceConnection(UsbDevice device) {
        if (device != null) {
            mDeviceConnectionLock.lock();
            try {
                mConnection = setupDevice(mManager, device);
                Log.i(TAG, "Connected to USB device with " +
                        mConnection);
            } catch(UsbDeviceException e) {
                Log.w("Couldn't open USB device", e);
            } finally {
                mDevicePermissionChanged.signal();
                mDeviceConnectionLock.unlock();
            }
        } else {
            Log.d(TAG, "Permission denied for device " + device);
        }
    }

    private void disconnectDevice() {
        if(mConnection != null) {
            Log.d(TAG, "Closing connection " + mConnection +
                    " with USB device");
            mDeviceConnectionLock.lock();
            mConnection.close();
            mConnection = null;
            mDeviceConnectionLock.unlock();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(
                        UsbManager.EXTRA_DEVICE);

                if(intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    openDeviceConnection(device);
                } else {
                    Log.i(TAG, "User declined permission for device " +
                            device);
                }
            } else if(ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d(TAG, "Device attached");
                try {
                    setupDevice(mManager, mVendorId, mProductId);
                } catch(DataSourceException e) {
                    Log.i(TAG, "Unable to load USB device -- waiting for it " +
                            "to appear", e);
                }
            } else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.d(TAG, "Device detached");
                disconnectDevice();
            }
        }
    };
}
