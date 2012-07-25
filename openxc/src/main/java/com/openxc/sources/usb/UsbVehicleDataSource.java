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

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.openxc.controllers.VehicleController;

import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.NoValueException;

import com.openxc.remote.RawMeasurement;

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
        implements Runnable, VehicleController {
    private static final String TAG = "UsbVehicleDataSource";
    private static final int ENDPOINT_COUNT = 2;
    public static final String ACTION_USB_PERMISSION =
            "com.ford.openxc.USB_PERMISSION";
    public static final String ACTION_USB_DEVICE_ATTACHED =
            "com.ford.openxc.USB_DEVICE_ATTACHED";

    private boolean mRunning;
    private UsbManager mManager;
    private UsbDeviceConnection mConnection;
    private UsbInterface mInterface;
    private UsbEndpoint mInEndpoint;
    private UsbEndpoint mOutEndpoint;
    private PendingIntent mPermissionIntent;
    private final URI mDeviceUri;
    private final Lock mDeviceConnectionLock;
    private final Condition mDeviceChanged;
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

        mDeviceUri = device;
        mDeviceConnectionLock = new ReentrantLock();
        mDeviceChanged = mDeviceConnectionLock.newCondition();

        try {
            mManager = (UsbManager) getContext().getSystemService(
                    Context.USB_SERVICE);
        } catch(NoClassDefFoundError e) {
            String message = "No USB service found on this device -- " +
                "can't use USB vehicle interface";
            Log.w(TAG, message);
            throw new DataSourceException(message);
        }
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
        start();
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

    public UsbVehicleDataSource(Context context) throws DataSourceException {
        this(null, context);
    }

    public synchronized void start() {
        if(!mRunning) {
            initializeDevice();
            primeOutput();
            mRunning = true;
            new Thread(this).start();
        }
    }

    /**
     * Unregister USB device intent broadcast receivers and stop waiting for a
     * connection.
     *
     * This should be called before the object is given up to the garbage
     * collector to avoid leaking a receiver in the Android framework.
     */
    public void stop() {
        super.stop();
        Log.d(TAG, "Stopping USB listener");

        mDeviceConnectionLock.lock();
        mDeviceChanged.signal();
        if(mConnection != null && mInterface != null) {
            mConnection.releaseInterface(mInterface);
        }
        mDeviceConnectionLock.unlock();
        getContext().unregisterReceiver(mBroadcastReceiver);

        if(!mRunning) {
            Log.d(TAG, "Already stopped.");
            return;
        }
        mRunning = false;
    }

    /**
     * Continuously read JSON messages from an attached USB device, or wait for
     * a connection.
     *
     * This loop will only exit if {@link #stop()} is called - otherwise it
     * either waits for a new device connection or reads USB packets.
     */
    public void run() {
        double lastLoggedTransferStatsAtByte = 0;
        byte[] bytes = new byte[128];
        StringBuffer buffer = new StringBuffer();
        final long startTime = System.nanoTime();
        long endTime;
        while(mRunning) {
            mDeviceConnectionLock.lock();

            try {
                waitForDeviceConnection();
            } catch(InterruptedException e) {
                Log.d(TAG, "Interrupted while waiting for a device");
                break;
            }

            // TODO when there haven't been any USB transfers for a long time,
            // we can get stuck here. do we need a timeout so it retries after
            // USB wakes backup? Why does USB seem to go to sleep in the first
            // place?
            int received = mConnection.bulkTransfer(
                    mInEndpoint, bytes, bytes.length, 0);
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
            .add("in_endpoint", mInEndpoint)
            .add("out_endpoint", mOutEndpoint)
            .toString();
    }

    public void set(RawMeasurement command) {
        String message = command.serialize() + "\u0000";
        Log.d(TAG, "Writing message to USB: " + message);
        byte[] bytes = message.getBytes();
        write(bytes);
    }

    private void initializeDevice() {
        try {
            connectToDevice(mManager, mVendorId, mProductId);
        } catch(DataSourceException e) {
            Log.i(TAG, "Unable to load USB device -- " +
                    "waiting for it to appear", e);
        }
    }

    private void write(byte[] bytes) {
        if(mConnection != null && mOutEndpoint != null) {
            Log.d(TAG, "Writing bytes to USB: " + bytes);
            int transferred = mConnection.bulkTransfer(
                    mOutEndpoint, bytes, bytes.length, 0);
            if(transferred < 0) {
                Log.w(TAG, "Unable to write CAN message to USB endpoint, error "
                        + transferred);
            }
        } else {
            Log.w(TAG, "No OUT endpoint available on USB device, " +
                    "can't send write command");
        }
    }

    private void logTransferStats(final long startTime, final long endTime) {
        double kilobytesTransferred = mBytesReceived / 1000.0;
        long elapsedTime = TimeUnit.SECONDS.convert(
            System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        Log.i(TAG, "Transferred " + kilobytesTransferred + " KB in "
            + elapsedTime + " seconds at an average of " +
            kilobytesTransferred / elapsedTime + " KB/s");
    }

    /* You must have the mDeviceConnectionLock locked before calling this
     * function.
     */
    private void waitForDeviceConnection() throws InterruptedException {
        while(mRunning && mConnection == null) {
            Log.d(TAG, "Still no device available");
            mDeviceChanged.await();
        }
    }

    private void parseStringBuffer(StringBuffer buffer) {
        int newlineIndex = buffer.indexOf("\n");
        if(newlineIndex != -1) {
            final String messageString = buffer.substring(0, newlineIndex);
            buffer.delete(0, newlineIndex + 1);
            handleMessage(messageString);
        }
    }

    private void connectToDevice(UsbManager manager, int vendorId,
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
        UsbInterface iface = null;
        for(int i = 0; i < device.getInterfaceCount(); i++) {
            iface = device.getInterface(i);
            if(iface.getEndpointCount() == ENDPOINT_COUNT) {
                break;
            }
        }

        if(iface == null) {
            Log.w(TAG, "Unable to find a USB device interface with the " +
                    "expected number of endpoints (" + ENDPOINT_COUNT + ")");
            return null;
        }

        for(int i = 0; i < iface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = iface.getEndpoint(i);
            if(endpoint.getType() ==
                    UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if(endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    Log.d(TAG, "Found IN endpoint " + endpoint);
                    mInEndpoint = endpoint;
                } else {
                    Log.d(TAG, "Found OUT endpoint " + endpoint);
                    mOutEndpoint = endpoint;
                }
            }

            if(mInEndpoint != null && mOutEndpoint != null) {
                break;
            }
        }
        return openInterface(manager, device, iface);
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

    private UsbDeviceConnection openInterface(UsbManager manager,
            UsbDevice device, UsbInterface iface)
            throws UsbDeviceException {
        UsbDeviceConnection connection = manager.openDevice(device);
        if(connection == null) {
            throw new UsbDeviceException("Couldn't open a connection to " +
                    "device -- user may not have given permission");
        }
        mInterface = iface;
        connection.claimInterface(mInterface, true);
        return connection;
    }

    /**
     * TODO we oddly need to "prime" this endpoint from Android because the
     * first message we send, if it's over 1 packet in size, we only get the
     * last packet.
     */
    private void primeOutput() {
        Log.d(TAG, "Priming output endpoint");
        write(new String("prime\u0000").getBytes());
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
                mDeviceChanged.signal();
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
            mInEndpoint = null;
            mOutEndpoint = null;
            mInterface = null;
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
                    connectToDevice(mManager, mVendorId, mProductId);
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
