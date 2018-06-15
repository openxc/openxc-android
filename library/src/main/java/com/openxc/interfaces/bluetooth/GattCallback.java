package com.openxc.interfaces.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.openxc.messages.streamers.JsonStreamer;

import java.util.UUID;

/**
 * Created by AKUMA128 on 2/9/2018.
 */

public class GattCallback extends BluetoothGattCallback {

    private final static String TAG = GattCallback.class.getSimpleName();
    public static final String C5_OPENXC_BLE_SERVICE_UUID = "6800D38B-423D-4BDB-BA05-C9276D8453E1";
    public static final String C5_OPENXC_BLE_CHARACTERISTIC_NOTIFY_UUID = "6800D38B-5262-11E5-885D-FEFF819CDCE3";
    public static final String C5_OPENXC_BLE_DESCRIPTOR_NOTIFY_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String C5_OPENXC_BLE_CHARACTERISTIC_WRITE_UUID = "6800D38B-5262-11E5-885D-FEFF819CDCE2";

    private boolean isConnected = false;
    private boolean isDisconnected = false;
    private BluetoothGatt mBluetoothGatt;
    private StringBuffer messageBuffer = new StringBuffer();
    private final static String DELIMITER = "\u0000";

    public void setBluetoothGatt(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            setConnected(true);
            Log.d(TAG, "Status BLE Connected");
            // Attempts to discover services after successful connection.
            if (mBluetoothGatt != null) {
                boolean discoverServices = mBluetoothGatt.discoverServices();
                Log.i(TAG, "Attempting to start service discovery:" + discoverServices);
            } else {
                Log.d(TAG, "BluetoothGatt is null");
            }

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.d(TAG, "Status BLE Disconnected");
            setDisconnected(true);
            setConnected(false);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        Log.i("CharacteristicRead", characteristic.toString());

    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }

    public void setDisconnected(boolean disconnected) {
        isDisconnected = disconnected;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        readChangedCharacteristic(characteristic);
    }

    public void readChangedCharacteristic(BluetoothGattCharacteristic characteristic) {
        byte[] data;
        data = characteristic.getValue(); // *** this is going to get overwritten by next call, so make a queue

        if (data != null && data.length > 0 && (isConnected())) {
            messageBuffer.append(new String(data, 0, data.length));

            if(JsonStreamer.containsJson(messageBuffer.toString())) {
                int delimiterIndex = messageBuffer.indexOf(DELIMITER);
                if (delimiterIndex != -1) {
                    byte message[] = messageBuffer.substring(0, delimiterIndex + 1).getBytes();
                    BLEInputStream.getInstance().putDataInBuffer(message);
                    messageBuffer.delete(0, delimiterIndex + 1);
                }
            } else {
                BLEInputStream.getInstance().putDataInBuffer(data);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothGattService gattService = gatt.getService(UUID.fromString(C5_OPENXC_BLE_SERVICE_UUID));
            BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(UUID.fromString(C5_OPENXC_BLE_CHARACTERISTIC_NOTIFY_UUID));
            gatt.setCharacteristicNotification(gattCharacteristic, true);
            BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(UUID.fromString(C5_OPENXC_BLE_DESCRIPTOR_NOTIFY_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

}
