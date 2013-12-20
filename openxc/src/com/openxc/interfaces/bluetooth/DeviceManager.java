package com.openxc.interfaces.bluetooth;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * The DeviceManager collects the functions required to connect to and open a
 * socket to the Bluetooth device.
 *
 * The device must be previously bonded, as this class does not initiate
 * discovery.
 */
public class DeviceManager {
    private final static String TAG = "DeviceManager";
    private final static UUID RFCOMM_UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mTargetDevice;
    private final Lock mDeviceLock = new ReentrantLock();
    private BluetoothSocket mSocket;
    private AtomicBoolean mSocketConnecting = new AtomicBoolean(false);

    /**
     * The DeviceManager requires an Android Context in order to send the intent
     * to enable Bluetooth if it isn't already on.
     */
    public DeviceManager(Context context) throws BluetoothException {
        // work around an Android bug, requires that this is called before
        // getting the default adapter
        if(Looper.myLooper() == null) {
            Looper.prepare();
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            String message = "This device most likely does not have " +
                    "a Bluetooth adapter";
            Log.w(TAG, message);
            throw new BluetoothException(message);
        }
    }

    /**
     * Connect to the target device and open a socket. This method will block
     * while waiting for the device.
     *
     * Returns a socket connected to the device.
     */
    public BluetoothSocket connect(String targetAddress)
            throws BluetoothException {
        mDeviceLock.lock();
        try {
            mTargetDevice = mBluetoothAdapter.getRemoteDevice(targetAddress);
            if(mTargetDevice != null) {
                mSocket = setupSocket(mTargetDevice);
                connectToSocket(mSocket);
            } else {
                Log.e(TAG, "Unable to find Bluetooth device " + targetAddress);
            }
        } finally {
            mDeviceLock.unlock();
        }
        return mSocket;
    }

    private void connectToSocket(BluetoothSocket socket) throws BluetoothException {
        mSocketConnecting.set(true);
        try {
            socket.connect();
        } catch(IOException e) {
            String error = "Could not connect socket to SPP service on " +
                mTargetDevice;
            Log.e(TAG, error);
            try {
                socket.close();
            } catch(IOException e2) {}
            throw new BluetoothException(error, e);
        } finally {
            mSocketConnecting.set(false);
        }
    }

    /**
     * Open an RFCOMM socket to the connected Bluetooth device.
     *
     * The DeviceManager must already have a device connected, so connectDevice
     * needs to be called.
     */
    private BluetoothSocket setupSocket(BluetoothDevice device)
            throws BluetoothException {
        if(device == null) {
            Log.w(TAG, "Can't setup socket -- device is " + device);
            throw new BluetoothException();
        }

        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        Log.d(TAG, "Scanning services on " + device);
        BluetoothSocket socket = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(
                    RFCOMM_UUID);
        } catch(IOException e) {
            String error = "Unable to open a socket to device " + device;
            Log.w(TAG, error);
            throw new BluetoothException(error, e);
        }
        return socket;
    }

    /**
     * Immediately cancel any pending Bluetooth operations.
     *
     * The BluetoothSocket.connect() function blocks while waiting for a
     * connection, but it's thread safe and we can cancel that by calling
     * close() on it at any time.
     *
     * Importantly we don't want to close the socket any other time, becauase we
     * want to leave that up to the user of the socket - if you call close()
     * twice, or close Input/Ouput streams associated with the socket
     * simultaneously, it can cause a segfault due to a bug in some Android
     * Bluetooth stacks. Awesome!
     */
    public void stop() {
        if(mSocketConnecting.get() && mSocket != null) {
            try {
                mSocket.close();
            } catch(IOException e) { }
        }
    }
}
