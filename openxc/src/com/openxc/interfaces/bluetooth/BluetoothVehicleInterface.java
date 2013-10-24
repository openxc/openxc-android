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
        lockConnection();
        boolean connected = mSocket != null && super.isConnected();
        unlockConnection();
        return connected;
    }

    @Override
    public void stop() {
        mDeviceManager.stop();
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

        lockConnection();
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
            unlockConnection();
        }
    }

    protected int read(byte[] bytes) throws IOException {
        lockConnection();
        int bytesRead = -1;
        try {
            if(isConnected()) {
                bytesRead = mInStream.read(bytes, 0, bytes.length);
            }
        } finally {
            unlockConnection();
        }
        return bytesRead;
    }

    private boolean write(String message) {
        lockConnection();
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
            unlockConnection();
        }
        return success;
    }

    // the Bluetooth socket is thread safe, so we don't grab the connection lock
    // - we also want to forcefully break the connection NOW instead of waiting
    // for the lock if BT is going down
    protected void disconnect() {
        try {
            lockConnection();
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
            disconnected();
        } finally {
            unlockConnection();
        }
    }

    protected String getTag() {
        return TAG;
    }

    /**
     * You must have call lockConnection before using this function - this
     * method is private so we're letting the caller handle it.
     */
    private void connectStreams() throws BluetoothException {
        try {
            mOutStream = new BufferedWriter(new OutputStreamWriter(
                        mSocket.getOutputStream()));
            mInStream = new BufferedInputStream(mSocket.getInputStream());
            Log.i(TAG, "Socket stream to vehicle interface opened successfully");
        } catch(IOException e) {
            Log.e(TAG, "Error opening streams ", e);
            disconnect();
            throw new BluetoothException();
        }
    }

    private void setAddress(String address) {
        // TODO verify this is a valid MAC address
        mAddress = address;
    }

    private static boolean sameResource(String address, String otherAddress) {
        return otherAddress != null && otherAddress.equals(address);
    }
}
