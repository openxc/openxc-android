package com.openxc.interfaces.usb;

import java.io.IOException;
import java.net.URI;

import android.annotation.TargetApi;
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
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.openxc.interfaces.UriBasedVehicleInterfaceMixin;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.sources.BytestreamDataSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;
import com.openxc.sources.SourceCallback;

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
@TargetApi(12)
public class UsbVehicleInterface extends BytestreamDataSource
        implements VehicleInterface {
    private static final String TAG = "UsbVehicleInterface";
    private static final int ENDPOINT_COUNT = 2;

    public static final String ACTION_USB_PERMISSION =
            "com.ford.openxc.USB_PERMISSION";
    public static final String ACTION_USB_DEVICE_ATTACHED =
            "com.ford.openxc.USB_DEVICE_ATTACHED";

    private UsbManager mManager;
    private UsbDeviceConnection mConnection;
    private UsbInterface mInterface;
    private UsbEndpoint mInEndpoint;
    private UsbEndpoint mOutEndpoint;
    private PendingIntent mPermissionIntent;
    private URI mDeviceUri;

    /**
     * Construct an instance of UsbVehicleInterface with a receiver callback
     * and custom device URI.
     *
     * If the device cannot be found at initialization, the object will block
     * waiting for a signal to check again.
     *
     * @param context The Activity or Service context, used to get access to the
     *      Android UsbManager.
     * @param callback An object implementing the
     *      SourceCallback that should receive data as it is
     *      received and parsed.
     * @param deviceUri a USB device URI (see {@link UsbDeviceUtilities} for the
     *      format) to look for.
     * @throws DataSourceException  If the URI doesn't have the correct
     *          format
     */
    public UsbVehicleInterface(SourceCallback callback, Context context,
            URI deviceUri) throws DataSourceException {
        super(callback, context);
        mDeviceUri = createUri(deviceUri);

        try {
            mManager = (UsbManager) getContext().getSystemService(
                    Context.USB_SERVICE);
            if(mManager == null) {
                throw new NoClassDefFoundError();
            }
        } catch(NoClassDefFoundError e) {
            String message = "No USB service found on this device -- " +
                "can't use USB vehicle interface";
            Log.w(TAG, message);
            throw new DataSourceException(message);
        }

        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(ACTION_USB_PERMISSION), 0);

        start();
    }

    /**
     * Construct an instance of UsbVehicleInterface with a receiver callback
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
    public UsbVehicleInterface(SourceCallback callback, Context context)
            throws DataSourceException {
        this(callback, context, null);
    }

    public UsbVehicleInterface(Context context) throws DataSourceException {
        this(null, context);
    }

    public UsbVehicleInterface(Context context, String uriString)
            throws DataSourceException {
        this(null, context, createUri(uriString));
    }

    @Override
    public synchronized void start() {
        super.start();
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        initializeDevice();
    }

    @Override
    public boolean isConnected() {
        return mConnection != null && super.isConnected();
    }

    /**
     * Unregister USB device intent broadcast receivers and stop waiting for a
     * connection.
     *
     * This should be called before the object is given up to the garbage
     * collector to avoid leaking a receiver in the Android framework.
     */
    @Override
    public void stop() {
        super.stop();
        try {
            getContext().unregisterReceiver(mBroadcastReceiver);
        } catch(IllegalArgumentException e) {
            Log.d(TAG, "Unable to unregister receiver when stopping, probably not registered");
        }
    }

    @Override
    public boolean setResource(String otherUri) throws DataSourceException {
        if(mDeviceUri == UsbDeviceUtilities.DEFAULT_USB_DEVICE_URI
                    && otherUri != null &&
                !UriBasedVehicleInterfaceMixin.sameResource(mDeviceUri,
                    otherUri)) {
            mDeviceUri = createUri(otherUri);
            stop();
            start();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("device", mDeviceUri)
            .add("connection", mConnection)
            .add("in_endpoint", mInEndpoint)
            .add("out_endpoint", mOutEndpoint)
            .toString();
    }

    @Override
    protected int read(byte[] bytes) throws IOException {
        mConnectionLock.readLock().lock();
        int bytesRead = 0;
        try {
            if(isConnected()) {
                bytesRead = mConnection.bulkTransfer(mInEndpoint, bytes, bytes.length, 0);
            }
        } finally {
            mConnectionLock.readLock().unlock();
        }
        return bytesRead;
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    private void initializeDevice() {
        try {
            connectToDevice(mManager, mDeviceUri);
        } catch(DataSourceException e) {
            Log.i(TAG, "Unable to load USB device -- " +
                    "waiting for it to appear", e);
        }
    }

    protected boolean write(byte[] bytes) {
        if(mConnection != null) {
            if(mOutEndpoint != null) {
                int transferred = mConnection.bulkTransfer(
                        mOutEndpoint, bytes, bytes.length, 0);
                if(transferred < 0) {
                    Log.w(TAG, "Unable to write CAN message to USB endpoint, error "
                            + transferred);
                    return false;
                }
            } else {
                Log.w(TAG, "No OUT endpoint available on USB device, " +
                        "can't send write command");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void connectToDevice(UsbManager manager, URI deviceUri)
            throws DataSourceResourceException {
        connectToDevice(manager,
                UsbDeviceUtilities.vendorFromUri(deviceUri),
                UsbDeviceUtilities.productFromUri(deviceUri));
    }

    private void connectToDevice(UsbManager manager, int vendorId,
            int productId) throws DataSourceResourceException {
        UsbDevice device = findDevice(manager, vendorId, productId);
        if(manager.hasPermission(device)) {
            Log.d(TAG, "Already have permission to use " + device);
            openConnection(device);
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

    private void openConnection(UsbDevice device) {
        if (device != null) {
            mConnectionLock.writeLock().lock();
            try {
                mConnection = setupDevice(mManager, device);
                connected();
                Log.i(TAG, "Connected to USB device with " +
                        mConnection);
            } catch(UsbDeviceException e) {
                Log.w("Couldn't open USB device", e);
            } finally {
                mConnectionLock.writeLock().unlock();
            }
        } else {
            Log.d(TAG, "Permission denied for device " + device);
        }
    }

    @Override
    protected void connect() throws DataSourceException { }

    @Override
    protected void disconnect() {
        if(!isConnected()) {
            return;
        }

        Log.d(TAG, "Closing connection " + mConnection +
                " with USB device");
        mConnectionLock.writeLock().lock();
        try {
            if(mConnection != null) {
                mConnection.close();
            }
            mConnection = null;
            mInEndpoint = null;
            mOutEndpoint = null;
            mInterface = null;
            disconnected();
        } finally {
            mConnectionLock.writeLock().unlock();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION:
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openConnection(device);
                    } else {
                        Log.i(TAG, "User declined permission for device " +
                                device);
                    }
                    break;
                case ACTION_USB_DEVICE_ATTACHED:
                    Log.d(TAG, "Device attached");
                    try {
                        connectToDevice(mManager, mDeviceUri);
                    } catch (DataSourceException e) {
                        Log.i(TAG, "Unable to load USB device -- waiting for it " +
                                "to appear", e);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.d(TAG, "Device detached");
                    disconnect();
                    break;
            }
        }
    };

    public static URI createUri(String uriString) throws DataSourceException {
        URI uri;
        if(uriString == null) {
            uri = null;
        } else {
            uri = UriBasedVehicleInterfaceMixin.createUri(uriString);
        }
        return createUri(uri);
    }

    private static URI createUri(URI uri) throws DataSourceResourceException {
        if(uri == null || uri.toString().isEmpty()) {
            uri = UsbDeviceUtilities.DEFAULT_USB_DEVICE_URI;
            Log.i(TAG, "No USB device specified -- using default " +
                    uri);
        }

        if(!validateResource(uri)) {
            throw new DataSourceResourceException(
                    "USB device URI must have the usb:// scheme");
        }

        // will throw an exception if not in the correct format
        UsbDeviceUtilities.vendorFromUri(uri);
        UsbDeviceUtilities.productFromUri(uri);

        return uri;
    }

    private static boolean validateResource(URI uri) {
        return uri.getScheme() != null && uri.getScheme().equals("usb");
    }
}
