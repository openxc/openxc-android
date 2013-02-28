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
            stop();
            start();
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "Stopping Bluetooth interface");
        disconnect();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("deviceAddress", mAddress)
            .add("socket", mSocket)
            .toString();
    }

    protected int read(byte[] bytes) throws IOException {
        return mInStream.read(bytes, 0, bytes.length);
    }

    protected void disconnect() {
        if(mSocket == null) {
            Log.w(TAG, "Unable to disconnect -- not connected");
            return;
        }

        Log.d(TAG, "Disconnecting from the socket " + mSocket);
        try {
            if(mInStream != null) {
                mInStream.close();
                mInStream = null;
            }
        } catch(IOException e) {
            Log.w(TAG, "Unable to close the input stream", e);
        }

        try {
            if(mOutStream != null) {
                mOutStream.close();
                mOutStream = null;
            }
        } catch(IOException e) {
            Log.w(TAG, "Unable to close the output stream", e);
        }

        if(mSocket != null) {
            try {
                mSocket.close();
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the socket", e);
            }
        }
        mSocket = null;

        disconnected();
        Log.d(TAG, "Disconnected from the socket");
    }

    protected String getTag() {
        return TAG;
    }

    protected void waitForConnection() throws DataSourceException {
        if(mSocket == null) {
            try {
                mSocket = mDeviceManager.connect(mAddress);
                connectStreams();
                connected();
            } catch(BluetoothException e) {
                String message = "Unable to connect to device at address "
                    + mAddress;
                Log.w(TAG, message, e);
                disconnected();
                throw new DataSourceException(message, e);
            }
        }
    }

    private synchronized boolean write(String message) {
        if(mSocket == null) {
            Log.w(TAG, "Unable to write -- not connected");
            return false;
        }

        try {
            Log.d(TAG, "Writing message to Bluetooth: " + message);
            mOutStream.write(message);
            // TODO what if we didn't flush every time? might be faster for
            // sustained writes.
            mOutStream.flush();
        } catch(IOException e) {
            Log.d(TAG, "Error writing to stream", e);
            return false;
        }
        return true;
    }

    private void connectStreams() throws BluetoothException {
        try {
            mOutStream = new BufferedWriter(new OutputStreamWriter(
                        mSocket.getOutputStream()));
            mInStream = new BufferedInputStream(mSocket.getInputStream());
            Log.i(TAG, "Socket stream to CAN translator opened successfully");
        } catch(IOException e) {
            Log.e(TAG, "Error opening streams ", e);
            mSocket = null;
            disconnected();
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
