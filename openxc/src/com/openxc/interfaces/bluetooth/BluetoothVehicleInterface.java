package com.openxc.interfaces.bluetooth;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.remote.RawMeasurement;
import com.openxc.sources.BytestreamDataSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

/**
 * A vehicle data source reading measurements from an Bluetooth-enabled
 * OpenXC device.
 *
 * This class tries to connect to a previously paired Bluetooth device with a
 * given MAC address. If found, it opens a socket to the device and streams
 * both read and write OpenXC messages.
 *
 * This class requires both the android.permission.BLUETOOTH and
 * android.permission.BLUETOOTH_ADMIN permissions.
 */
public class BluetoothVehicleInterface extends BytestreamDataSource
        implements VehicleInterface {
    private static final String TAG = "BluetoothVehicleInterface";
    public static final String DEVICE_NAME_PREFIX = "OpenXC-VI-";

    private DeviceManager mDeviceManager;
    private String mAddress;
    private BufferedWriter mOutStream;
    private BufferedInputStream mInStream;
    private BluetoothSocket mSocket;

    public BluetoothVehicleInterface(SourceCallback callback, Context context,
            String address) throws DataSourceException {
        super(callback, context);
        try {
            mDeviceManager = new DeviceManager(getContext());
        } catch(BluetoothException e) {
            throw new DataSourceException(
                    "Unable to open Bluetooth device manager", e);
        }

        setAddress(address);
        start();
    }

    public BluetoothVehicleInterface(Context context, String address)
            throws DataSourceException {
        this(null, context, address);
    }

    public boolean receive(RawMeasurement command) {
        String message = command.serialize() + "\u0000";
        return write(message);
    }

    public boolean setResource(String otherAddress) throws DataSourceException {
        if(!sameResource(mAddress, otherAddress)) {
            setAddress(otherAddress);
            try {
                if(mSocket != null) {
                    mSocket.close();
                }
            } catch(IOException e) {
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        mConnectionLock.readLock().lock();

        boolean connected = super.isConnected();
        if(mSocket == null) {
            connected = false;
        } else {
            try {
                connected &= mSocket.isConnected();
            } catch (NoSuchMethodError e) {
                // Cannot get isConnected() result before API 14
                // Assume previous result is correct.
            }
        }

        mConnectionLock.readLock().unlock();
        return connected;
    }

    @Override
    public void stop() {
        mDeviceManager.stop();
        closeSocket();
        super.stop();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("deviceAddress", mAddress)
            .add("socket", mSocket)
            .toString();
    }

    protected void connect() {
        if(!isRunning()) {
            return;
        }

        mConnectionLock.writeLock().lock();
        try {
            if(!isConnected()) {
                mSocket = mDeviceManager.connect(mAddress);
                connectStreams();
                connected();
            }
        } catch(BluetoothException e) {
            String message = "Unable to connect to device at address "
                + mAddress;
            Log.w(TAG, message, e);
            disconnected();
        } finally {
            mConnectionLock.writeLock().unlock();
        }
    }

    protected int read(byte[] bytes) throws IOException {
        mConnectionLock.readLock().lock();
        int bytesRead = -1;
        try {
            if(isConnected()) {
                bytesRead = mInStream.read(bytes, 0, bytes.length);
            }
        } finally {
            mConnectionLock.readLock().unlock();
        }
        return bytesRead;
    }

    private boolean write(String message) {
        mConnectionLock.readLock().lock();
        boolean success = false;
        try {
            if(isConnected()) {
                Log.d(TAG, "Writing message to Bluetooth: " + message);
                mOutStream.write(message);
                // TODO what if we didn't flush every time? might be faster for
                // sustained writes.
                mOutStream.flush();
                success = true;
            } else {
                Log.w(TAG, "Unable to write -- not connected");
            }
        } catch(IOException e) {
            Log.d(TAG, "Error writing to stream", e);
        } finally {
            mConnectionLock.readLock().unlock();
        }
        return success;
    }

    private synchronized void closeSocket() {
        // The Bluetooth socket is thread safe, so we don't grab the connection
        // lock - we also want to forcefully break the connection NOW instead of
        // waiting for the lock if BT is going down
        try {
            if(mSocket != null) {
                mSocket.close();
                Log.d(TAG, "Disconnected from the socket");
            }
        } catch(IOException e) {
            Log.w(TAG, "Unable to close the socket", e);
        } finally {
            mSocket = null;
        }
    }

    protected void disconnect() {
        closeSocket();
        mConnectionLock.writeLock().lock();
        try {
            try {
                if(mInStream != null) {
                    mInStream.close();
                    Log.d(TAG, "Disconnected from the input stream");
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the input stream", e);
            } finally {
                mInStream = null;
            }

            try {
                if(mOutStream != null) {
                    mOutStream.close();
                    Log.d(TAG, "Disconnected from the output stream");
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the output stream", e);
            } finally {
                mOutStream = null;
            }

            disconnected();
        } finally {
            mConnectionLock.writeLock().unlock();
        }
    }

    protected String getTag() {
        return TAG;
    }

    private void connectStreams() throws BluetoothException {
        mConnectionLock.writeLock().lock();
        try {
            try {
                mOutStream = new BufferedWriter(new OutputStreamWriter(
                            mSocket.getOutputStream()));
                mInStream = new BufferedInputStream(mSocket.getInputStream());
                Log.i(TAG, "Socket stream to vehicle interface " +
                        "opened successfully");
            } catch(IOException e) {
                Log.e(TAG, "Error opening streams ", e);
                disconnect();
                throw new BluetoothException();
            }
        } finally {
            mConnectionLock.writeLock().unlock();
        }
    }

    private void setAddress(String address) {
        if(address == null || !BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new DataSourceResourceException("MAC is not valid");
        }
        mAddress = address;
    }

    private static boolean sameResource(String address, String otherAddress) {
        return otherAddress != null && otherAddress.equals(address);
    }
}
