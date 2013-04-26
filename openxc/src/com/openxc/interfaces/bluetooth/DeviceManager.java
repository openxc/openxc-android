package com.openxc.interfaces.bluetooth;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Looper;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
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

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mTargetDevice;
    private final Lock mDeviceLock = new ReentrantLock();
    private final Condition mDeviceChangedCondition =
            mDeviceLock.newCondition();
    private BroadcastReceiver mReceiver;

    /**
     * The DeviceManager requires an Android Context in order to send the intent
     * to enable Bluetooth if it isn't already on.
     */
    public DeviceManager(Context context) throws BluetoothException {
        mContext = context;
        // work around an Android bug, requires that this is called before
        // getting the default adapter
        Looper.prepare();
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
        connectDevice(targetAddress);
        mDeviceLock.lock();
        while(mTargetDevice == null) {
            try {
                mDeviceChangedCondition.await();
            } catch(InterruptedException e) {}
        }
        BluetoothSocket socket = setupSocket(mTargetDevice);
        mDeviceLock.unlock();
        return socket;
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

        try {
            socket.connect();
        } catch(IOException e) {
            String error = "Could not connect socket to SPP service on " +
                device;
            Log.e(TAG, error);
            try {
                socket.close();
            } catch(IOException e2) {}
            throw new BluetoothException(error, e);
        }
        return socket;
    }

    private void captureDevice(BluetoothDevice device) {
        mDeviceLock.lock();
        mTargetDevice = device;
        mDeviceChangedCondition.signal();
        mDeviceLock.unlock();

        if(mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            if(mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    }

    /** Check if a Bluetooth device matches the target address we're looking
     * for.
     *
     * @param device candidate Bluetooth device that's been previously bonded
     * @param targetAddress Bluetooth MAC addres we're looking for
     *
     * @return true if the address matches the candidate device
     */
    private boolean checkCandidateDevice(BluetoothDevice device,
            String targetAddress) {
        Log.d(TAG, "Found Bluetooth device: " + device);
        if(device.getAddress().equals(targetAddress)) {
            Log.d(TAG, "Found matching device: " + device);
            return true;
        }
        return false;
    }

    /**
     * Check the list of previously paired devices for one matching the target
     * address. Once a matching device is found, calls captureDevice to connect
     * with it.
     *
     * This will not attempt to pair with unpaired devices - it's assumed that
     * this step has already been completed by the user when selecting the
     * Bluetooth device to use. If this class is used programatically with a
     * hard-coded target address, you'll need to have previously paired the
     * device.
     */
    private void connectDevice(final String targetAddress) {
        Log.d(TAG, "Starting device enumeration");
        Set<BluetoothDevice> pairedDevices =
            mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices) {
            Log.d(TAG, "Found already paired device: " + device);
            if(checkCandidateDevice(device, targetAddress)) {
                captureDevice(device);
                return;
            }
        }
    }
}
