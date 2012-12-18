package com.openxc.sources.bluetooth;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.controllers.VehicleController;
import com.openxc.remote.RawMeasurement;
import com.openxc.sources.BytestreamDataSourceMixin;
import com.openxc.sources.ContextualVehicleDataSource;
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
public class BluetoothVehicleDataSource extends ContextualVehicleDataSource
        implements Runnable, VehicleController {
    private static final String TAG = "BluetoothVehicleDataSource";

    private boolean mRunning = false;
    private DeviceManager mDeviceManager;
    private BufferedWriter mOutStream;
    private BufferedInputStream mInStream;
    private BluetoothSocket mSocket;
    private String mAddress;

    public BluetoothVehicleDataSource(SourceCallback callback, Context context,
            String address) throws DataSourceException {
        super(callback, context);
        try {
            mDeviceManager = new DeviceManager(getContext());
        } catch(BluetoothException e) {
            throw new DataSourceException(
                    "Unable to open Bluetooth device manager", e);
        }
        mAddress = address;
        start();
    }

    public BluetoothVehicleDataSource(Context context, String address)
            throws DataSourceException {
        this(null, context, address);
    }

    public synchronized void start() {
        if(!mRunning) {
            mRunning = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        super.stop();
        if(!mRunning) {
            Log.d(TAG, "Already stopped.");
            return;
        }
        Log.d(TAG, "Stopping Bluetooth source");
        mRunning = false;
    }

    public synchronized void close() {
        stop();
        disconnect();
    }

    // TODO this could be made generic so we could use any standard serial
    // device, e.g. xbee or FTDI
    public void run() {
        BytestreamDataSourceMixin buffer = new BytestreamDataSourceMixin();
        while(mRunning) {
            try {
                waitForDeviceConnection();
            } catch(BluetoothException e) {
                Log.i(TAG, "Unable to connect to target device -- " +
                        "sleeping for awhile before trying again");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e2){
                    stop();
                }
                continue;
            }

            int received;
            byte[] bytes = new byte[512];
            try {
                received = mInStream.read(bytes, 0, bytes.length);
            } catch(IOException e) {
                Log.e(TAG, "Unable to read response");
                disconnect();
                continue;
            }

            if(received > 0) {
                buffer.receive(bytes, received);
                for(String record : buffer.readLines()) {
                    handleMessage(record);
                }
            }
        }
        Log.d(TAG, "Stopped Bluetooth listener");
    }

    public void set(RawMeasurement command) {
        String message = command.serialize() + "\u0000";
        Log.d(TAG, "Writing message to Bluetooth: " + message);
        try {
            write(message);
        } catch(BluetoothException e) {
            Log.w(TAG, "Unable to write message", e);
        }
    }

    public String getAddress() {
        return mAddress;
    }

    protected void disconnect() {
        if(mSocket == null) {
            Log.w(TAG, "Unable to disconnect -- not connected");
            return;
        }

        Log.d(TAG, "Disconnecting from the socket " + mSocket);
        try {
            mOutStream.close();
            mInStream.close();
        } catch(IOException e) {
            Log.w(TAG, "Unable to close the input stream", e);
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

    private synchronized void write(String message) throws BluetoothException {
        if(mSocket == null) {
            Log.w(TAG, "Unable to write -- not connected");
            throw new BluetoothException();
        }

        try {
            mOutStream.write(message);
            mOutStream.flush();
        } catch(IOException e) {
            Log.d(TAG, "Error writing to stream", e);
        }
    }

    private void waitForDeviceConnection() throws BluetoothException {
        if(mSocket == null) {
            try {
                mSocket = mDeviceManager.connect(mAddress);
                connected();
                connectStreams();
            } catch(BluetoothException e) {
                Log.w(TAG, "Unable to connect to device at address " +
                        mAddress, e);
                throw e;
            }
        }
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("deviceAddress", mAddress)
            .add("socket", mSocket)
            .toString();
    }
}
