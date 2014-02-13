package com.openxc.interfaces.bluetooth;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.remote.RawMeasurement;
import com.openxc.sources.BytestreamDataSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;
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
    private String mExplicitAddress;
    private String mConnectedAddress;
    private BufferedWriter mOutStream;
    private BufferedInputStream mInStream;
    private BluetoothSocket mSocket;
    private boolean mAutomaticMode = false;

    public BluetoothVehicleInterface(SourceCallback callback, Context context,
            String address) throws DataSourceException {
        super(callback, context);
        try {
            mDeviceManager = new DeviceManager(getContext());
        } catch(BluetoothException e) {
            throw new DataSourceException(
                    "Unable to open Bluetooth device manager", e);
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(mDiscoveryReceiver, filter);

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
        boolean reconnect = false;
        if(isConnected() && otherAddress == null) {
            // switch to automatic but don't break the existing connection
            reconnect = false;
        } else if(!sameResource(mConnectedAddress, otherAddress)) {
            reconnect = true;
        }

        setAddress(otherAddress);

        if(reconnect) {
            try {
                if(mSocket != null) {
                    mSocket.close();
                }
            } catch(IOException e) {
            }
            resetConnectionAttempts(0, RECONNECTION_ATTEMPT_WAIT_TIME_S);
        }
        return reconnect;
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
    public synchronized void stop() {
        if(isRunning()) {
            try {
                getContext().unregisterReceiver(mDiscoveryReceiver);
            } catch(IllegalArgumentException e) {
                Log.w(TAG, "Broadcast receiver not registered but we expected it to be");
            }
            mDeviceManager.stop();
            closeSocket();
            super.stop();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("explicitDeviceAddress", mExplicitAddress)
            .add("connectedDeviceAddress", mConnectedAddress)
            .add("socket", mSocket)
            .toString();
    }

    protected void connect() {
        if(!isRunning()) {
            return;
        }

        BluetoothDevice lastConnectedDevice =
                mDeviceManager.getLastConnectedDevice();

        BluetoothSocket newSocket = null;
        if(mExplicitAddress != null || !mAutomaticMode) {
            String address = mExplicitAddress;
            if(address == null && lastConnectedDevice != null) {
                address = lastConnectedDevice.getAddress();
            }

            if(address != null) {
                Log.i(TAG, "Connecting to Bluetooth device " + address);
                try {
                    if(!isConnected()) {
                        newSocket = mDeviceManager.connect(address);
                    }
                } catch(BluetoothException e) {
                    Log.w(TAG, "Unable to connect to device " + address, e);
                    newSocket = null;
                }
            } else {
                Log.d(TAG, "No detected or stored Bluetooth device MAC, not attempting connection");
            }
        } else {
            // Only try automatic detection of VI once, and whether or not we find
            // and connect to one, don't go into automatic mode again unless
            // manually triggered.
            mAutomaticMode = false;
            Log.v(TAG, "Attempting automatic detection of Bluetooth VI");

            ArrayList<BluetoothDevice> candidateDevices =
                new ArrayList<BluetoothDevice>(
                        mDeviceManager.getCandidateDevices());

            if(lastConnectedDevice != null) {
                Log.v(TAG, "First trying last connected BT VI: " +
                        lastConnectedDevice);
                candidateDevices.add(0, lastConnectedDevice);
            }

            for(BluetoothDevice device : candidateDevices) {
                try {
                    if(!isConnected()) {
                        Log.i(TAG, "Attempting connection to auto-detected " +
                                "VI " + device);
                        newSocket = mDeviceManager.connect(device);
                        break;
                    }
                } catch(BluetoothException e) {
                    Log.w(TAG, "Unable to connect to auto-detected device " +
                            device, e);
                    newSocket = null;
                }
            }

            if(lastConnectedDevice == null && newSocket == null
                    && candidateDevices.size() > 0) {
                Log.i(TAG, "No BT VI ever connected, and none of " +
                        "discovered devices could connect - storing " +
                        candidateDevices.get(0).getAddress() +
                        " as the next one to try");
                mDeviceManager.storeLastConnectedDevice(
                        candidateDevices.get(0));
            }
        }

        mConnectionLock.writeLock().lock();
        try {
            mSocket = newSocket;
            if(mSocket != null) {
                try {
                    connectStreams();
                    connected();
                    mConnectedAddress = mSocket.getRemoteDevice().getAddress();
                } catch(BluetoothException e) {
                    Log.d(TAG, "Unable to open Bluetooth streams", e);
                    disconnected();
                }
            } else {
                disconnected();
            }
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

    private void setAddress(String address) throws DataSourceResourceException {
        if(address != null && !BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new DataSourceResourceException("\"" + address +
                    "\" is not a valid MAC address");
        }
        mExplicitAddress = address;
    }

    private static boolean sameResource(String address, String otherAddress) {
        return otherAddress != null && otherAddress.equals(address);
    }

    private BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // Whenever discovery finishes, take the opportunity to try and
            // connect to detected devices if we're not already connected.
            // Discovery may have been initiated by the Enabler UI, or by some
            // other user action or app.
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                Log.d(TAG, "Bluetooth discovery has finished");
                if(!isConnected()) {
                    if(mExplicitAddress == null) {
                        mAutomaticMode = true;
                    }
                    resetConnectionAttempts(0, RECONNECTION_ATTEMPT_WAIT_TIME_S);
                }
            }
        }
    };

}
