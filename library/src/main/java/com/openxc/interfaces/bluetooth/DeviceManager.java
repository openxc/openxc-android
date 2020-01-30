package com.openxc.interfaces.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.openxc.util.SupportSettingsUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The DeviceManager collects the functions required to connect to and open a
 * socket to the Bluetooth device.
 * <p>
 * The device must be previously bonded, as this class does not initiate
 * discovery.
 */
public class DeviceManager {
    private final static String TAG = "DeviceManager";
    public static final String KNOWN_BLUETOOTH_DEVICE_PREFERENCES = "known_bluetooth_devices";
    public static final String KNOWN_BLUETOOTH_DEVICE_PREF_KEY = "known_bluetooth_devices";
    public static final String LAST_CONNECTED_BLUETOOTH_DEVICE_PREF_KEY = "last_connected_bluetooth_device";
    public final static UUID RFCOMM_UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private AtomicBoolean mSocketConnecting = new AtomicBoolean(false);
    private Context mContext;
    private BluetoothGatt mBluetoothGatt;
    private GattCallback mGattCallback;

    public static final int MAX_WRITE_BUFFER_CAPACITY = 1024;
    private byte[] writeArray = new byte[MAX_WRITE_BUFFER_CAPACITY];
    private int queueEnd = 0;
    public static final int PACKET_SENDING_WAIT_TIME_MS = 50;


    /**
     * The DeviceManager requires an Android Context in order to send the intent
     * to enable Bluetooth if it isn't already on.
     */
    public DeviceManager(Context context) throws BluetoothException {
        mContext = context;
        if (getDefaultAdapter() == null) {
            String message = "This device most likely does not have " +
                    "a Bluetooth adapter";
            Log.w(TAG, message);
            throw new BluetoothException(message);
        } else {
            Log.d(TAG, "Initializing Bluetooth device manager");
        }
    }

    private BluetoothAdapter getDefaultAdapter() {
        if (mBluetoothAdapter == null) {
            // work around an Android bug, requires that this is called before
            // getting the default adapter
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter;
    }

    public void startDiscovery() {
        if (getDefaultAdapter() != null) {
            if (getDefaultAdapter().isDiscovering()) {
                getDefaultAdapter().cancelDiscovery();
            }
            Log.i(TAG, "Starting Bluetooth discovery");
            getDefaultAdapter().startDiscovery();
        }
    }

    public BluetoothServerSocket listen() {
        BluetoothServerSocket tmp = null;
        try {
            // TODO use an OpenXC-specific UUID
            tmp = getDefaultAdapter().listenUsingRfcommWithServiceRecord(
                    "TODO", DeviceManager.RFCOMM_UUID);
        } catch (IOException e) {
        }
        return tmp;
    }

    /**
     * Connect to the target device and open a socket. This method will block
     * while waiting for the device.
     * <p>
     * Returns a socket connected to the device.
     */
    public BluetoothSocket connect(String targetAddress)
            throws BluetoothException {
        return connect(getDefaultAdapter().getRemoteDevice(targetAddress));
    }

    public BluetoothSocket connect(BluetoothDevice device)
            throws BluetoothException {
        if (device == null) {
            throw new BluetoothException("Not connecting to null Bluetooth device");
        }
        mSocket = setupSocket(device);
        connectToSocket(mSocket);

        storeLastConnectedDevice(device);
        return mSocket;
    }

    /**
     * Immediately cancel any pending Bluetooth operations.
     * <p>
     * The BluetoothSocket.connect() function blocks while waiting for a
     * connection, but it's thread safe and we can cancel that by calling
     * close() on it at any time.
     * <p>
     * Importantly we don't want to close the socket any other time, because we
     * want to leave that up to the user of the socket - if you call close()
     * twice, or close Input/Output streams associated with the socket
     * simultaneously, it can cause a segfault due to a bug in some Android
     * Bluetooth stacks. Awesome!
     */
    public void stop() {
        if (mSocketConnecting.get() && mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
        }
        if (mSocketConnecting.get() && mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        if (getDefaultAdapter() != null) {
            getDefaultAdapter().cancelDiscovery();
        }
    }

    public Set<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> devices = new HashSet<>();
        if (getDefaultAdapter() != null && getDefaultAdapter().isEnabled()) {
            devices = getDefaultAdapter().getBondedDevices();
        }
        return devices;
    }

    public void storeLastConnectedDevice(BluetoothDevice device) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                mContext.getApplicationContext()).edit();
        editor.putString(LAST_CONNECTED_BLUETOOTH_DEVICE_PREF_KEY,
                device.getAddress());
        editor.apply();
        Log.d(TAG, "Stored last connected device: " + device.getAddress());
    }

    public BluetoothDevice getLastConnectedDevice() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        String lastConnectedDeviceAddress = preferences.getString(
                LAST_CONNECTED_BLUETOOTH_DEVICE_PREF_KEY, null);
        BluetoothDevice lastConnectedDevice = null;
        if (lastConnectedDeviceAddress != null) {
            lastConnectedDevice = getDefaultAdapter().getRemoteDevice(lastConnectedDeviceAddress);
        }
        return lastConnectedDevice;
    }

    public Set<BluetoothDevice> getCandidateDevices() {
        Set<BluetoothDevice> candidates = new HashSet<>();

        for (BluetoothDevice device : getPairedDevices()) {
            if (device.getName().startsWith(
                    BluetoothVehicleInterface.DEVICE_NAME_PREFIX)) {
                candidates.add(device);
            }
        }

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());;
        Set<String> detectedDevices = SupportSettingsUtils.getStringSet(
                preferences, KNOWN_BLUETOOTH_DEVICE_PREF_KEY,
                new HashSet<String>());
        for (String address : detectedDevices) {
            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                candidates.add(getDefaultAdapter().getRemoteDevice(address));
            }
        }

        for (BluetoothDevice candidate : candidates) {
            Log.d(TAG, "Found previously discovered or paired OpenXC BT VI "
                    + candidate.getAddress());
        }
        return candidates;
    }

    public boolean isConnecting() {
        return mSocketConnecting.get();
    }

    private void connectToSocket(BluetoothSocket socket) throws BluetoothException {
        mSocketConnecting.set(true);
        try {
            socket.connect();
            if (getDefaultAdapter().isDiscovering()) {
                getDefaultAdapter().cancelDiscovery();
            }
        } catch (IOException e) {
            String error = "Could not connect to SPP service on " + socket;
            Log.e(TAG, error);
            try {
                socket.close();
            } catch (IOException e2) {
            }
            throw new BluetoothException(error, e);
        } finally {
            mSocketConnecting.set(false);
        }
    }

    /**
     * Open an RFCOMM socket to the Bluetooth device.
     * <p>
     * The device may or may not actually exist, the argument is just a
     * reference to it.
     */
    private BluetoothSocket setupSocket(BluetoothDevice device)
            throws BluetoothException {
        if (device == null) {
            Log.w(TAG, "Can't setup socket -- device is null");
            throw new BluetoothException();
        }

        Log.d(TAG, "Scanning services on " + device);
        BluetoothSocket socket;
        try {
            socket = device.createRfcommSocketToServiceRecord(RFCOMM_UUID);
        } catch (IOException e) {
            String error = "Unable to open a socket to device " + device;
            Log.w(TAG, error);
            throw new BluetoothException(error, e);
        }

        return socket;
    }

    public boolean isBLEDevice(String address) {

        return getDefaultAdapter().
                getRemoteDevice(address).
                getType() == BluetoothDevice.DEVICE_TYPE_LE;
    }

    public boolean isBLEDevice(BluetoothDevice device) {
        return device.getType() == BluetoothDevice.DEVICE_TYPE_LE;
    }

    public BluetoothGatt connectBLE(String address) throws BluetoothException {
        return connectBLE(getDefaultAdapter().getRemoteDevice(address));
    }

    public BluetoothGatt connectBLE(BluetoothDevice device) throws BluetoothException {
        if (device == null) {
            throw new BluetoothException("Not connecting to null Bluetooth device");
        }
        mSocketConnecting.set(true);
        try {
            mGattCallback = new GattCallback();
            mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback);
            mBluetoothGatt.connect();
            mGattCallback.setBluetoothGatt(mBluetoothGatt);
            Log.d(TAG, "Connected to gatt " + mBluetoothGatt);
            if (getDefaultAdapter().isDiscovering()) {
                getDefaultAdapter().cancelDiscovery();
            }
        } catch (IllegalArgumentException e) {
            String error = "Unable to connect to GATT Server on BLE device " + device;
            Log.w(TAG, error);
            mBluetoothGatt.disconnect();
            throw new BluetoothException(error, e);
        } finally {
            mSocketConnecting.set(false);
        }
        //storeLastConnectedDevice(device);
        return mBluetoothGatt;
    }

    /***
     * This is used to get the connection status using gatt callback
     * @return boolean value indicating
     */
    public boolean isBLEConnected() {
        if (mGattCallback != null) {
            return mGattCallback.isConnected();
        } else {
            return false;
        }
    }

    public boolean isBLEDisconnected() {
        if (mGattCallback != null) {
            return mGattCallback.isDisconnected();
        } else {
            return false;
        }
    }

    public boolean writeCharacteristicToBLE(byte[] bytes) {
        try {
            for (int i = 0; i < bytes.length; i++) {
                writeArray[queueEnd++] = bytes[i];
            }
        } catch (BufferOverflowException e) {
            Log.d(TAG, "Buffer overflowing!!!!");
            return false;
        }

        BLESendData();
        return true;
    }

    private void BLESendData() {
        if (queueEnd <= 0) {
            return;
        }
        if (mBluetoothGatt != null) {
            BluetoothGattService openXCService = mBluetoothGatt.getService(UUID.fromString(GattCallback.C5_OPENXC_BLE_SERVICE_UUID));
            if (openXCService != null) {
                BluetoothGattCharacteristic characteristic = openXCService.getCharacteristic(UUID.fromString(GattCallback.C5_OPENXC_BLE_CHARACTERISTIC_WRITE_UUID));
                if (characteristic != null) {

                    while (queueEnd != 0) {
                        byte[] sendingPacket;
                        if (queueEnd >= 20) {
                            sendingPacket = new byte[20];
                            System.arraycopy(writeArray, 0, sendingPacket, 0, 20);
                            System.arraycopy(writeArray, 20, writeArray, 0, queueEnd - 20);
                            queueEnd = queueEnd - 20;

                        } else {
                            sendingPacket = new byte[queueEnd];
                            System.arraycopy(writeArray, 0, sendingPacket, 0, queueEnd);
                            queueEnd = 0;
                        }
                        characteristic.setValue(sendingPacket);
                        try {
                            Thread.sleep(PACKET_SENDING_WAIT_TIME_MS);

                        } catch (InterruptedException e) {
                            Log.d(TAG, "Interrupted");
                            e.printStackTrace();
                        }
                        mBluetoothGatt.writeCharacteristic(characteristic);
                    }
                } else {
                    Log.d(TAG, "characteristic is null");
                }
            } else {
                Log.d(TAG, "OpenXC Service not found!");
            }
        } else {
            Log.d(TAG, "Gatt not found!");
        }
    }
}
