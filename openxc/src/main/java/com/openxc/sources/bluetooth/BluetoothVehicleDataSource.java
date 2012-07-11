package com.openxc.sources.bluetooth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.openxc.controllers.VehicleController;

import com.openxc.remote.RawMeasurement;

import com.openxc.sources.ContextualVehicleDataSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

import android.bluetooth.BluetoothSocket;

import android.content.Context;

import android.util.Log;

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
    private PrintWriter mOutStream;
    private BufferedReader mInStream;
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

    public void stop() {
        super.stop();
        Log.d(TAG, "Stopping Bluetooth source");
        if(!mRunning) {
            Log.d(TAG, "Already stopped.");
            return;
        }
        mRunning = false;
    }

    public void close() {
        stop();
        disconnect();
    }

    // TODO this could be made generic so we could use any standard serial
    // device, e.g. xbee or FTDI
    public void run() {
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
            String line = null;
            try {
                line = mInStream.readLine();
            } catch(IOException e) {
                Log.e(TAG, "Unable to read response");
                disconnect();
                continue;
            }

            if(line == null){
                Log.e(TAG, "Device has dropped offline");
                disconnect();
                continue;
            }
            handleMessage(line);
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

    private void disconnect() {
        if(mSocket == null) {
            Log.w(TAG, "Unable to disconnect -- not connected");
            return;
        }

        Log.d(TAG, "Disconnecting from the socket " + mSocket);
        mOutStream.close();
        try {
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
        Log.d(TAG, "Disconnected from the socket");
    }

    private synchronized void write(String message) throws BluetoothException {
        if(mSocket == null) {
            Log.w(TAG, "Unable to write -- not connected");
            throw new BluetoothException();
        }

        mOutStream.write(message);
        mOutStream.flush();
    }

    private void waitForDeviceConnection() throws BluetoothException {
        if(mSocket == null) {
            try {
                mSocket = mDeviceManager.connect(mAddress);
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
            mOutStream = new PrintWriter(new OutputStreamWriter(
                        mSocket.getOutputStream()));
            mInStream = new BufferedReader(new InputStreamReader(
                        mSocket.getInputStream()));
            Log.i(TAG, "Socket stream to CAN translator opened successfully");
        } catch(IOException e) {
            // We are expecting to see "host is down" when repeatedly
            // autoconnecting
            if(!(e.toString().contains("Host is Down"))){
                Log.d(TAG, "Error opening streams "+e);
            } else {
                Log.e(TAG, "Error opening streams "+e);
            }
            mSocket = null;
            throw new BluetoothException();
        }
    }
}
