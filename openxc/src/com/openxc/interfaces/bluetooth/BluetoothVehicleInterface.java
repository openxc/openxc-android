package com.openxc.interfaces.bluetooth;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.base.MoreObjects;

import com.openxcplatform.R;
import com.openxc.interfaces.VehicleInterface;
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
    private Thread mAcceptThread;
    private String mExplicitAddress;
    private String mConnectedAddress;
    private BufferedWriter mOutStream;
    private BufferedInputStream mInStream;
    private BluetoothSocket mSocket;
    private boolean mPerformAutomaticScan = true;
    private boolean mUsePolling = false;
    private boolean mSocketAccepterRunning = true;

    public BluetoothVehicleInterface(SourceCallback callback, Context context,
            String address) throws DataSourceException {
        super(callback, context);
        try {
            mDeviceManager = new DeviceManager(getContext());
        } catch(BluetoothException e) {
            throw new DataSourceException(
                    "Unable to open Bluetooth device manager", e);
        }

        IntentFilter filter = new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(mBroadcastReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        getContext().registerReceiver(mBroadcastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        mUsePolling = preferences.getBoolean(
                context.getString(R.string.bluetooth_polling_key), true);
        Log.d(TAG, "Bluetooth device polling is " + (mUsePolling ? "enabled" : "disabled"));

        setAddress(address);
        start();

        mAcceptThread = new Thread(new SocketAccepter());
        mAcceptThread.start();
    }

    public BluetoothVehicleInterface(Context context, String address)
            throws DataSourceException {
        this(null, context, address);
    }

    /**
     * Control whether periodic polling is used to detect a Bluetooth VI.
     *
     * This class opens a Bluetooth socket and will accept incoming connections
     * from a VI that can act as the Bluetooth master. For VIs that are only
     * able to act as slave, we have to poll for a connection occasionally to
     * see if it's within range.
     */
    public void setPollingStatus(boolean enabled) {
        mUsePolling = enabled;
    }

    @Override
    public boolean setResource(String otherAddress) throws DataSourceException {
        boolean reconnect = false;
        if(isConnected()) {
            if(otherAddress == null) {
                // switch to automatic but don't break the existing connection
                reconnect = false;
            } else if(!sameResource(mConnectedAddress, otherAddress) &&
                    !sameResource(mExplicitAddress, otherAddress)) {
                reconnect = true;
            }
        }

        setAddress(otherAddress);

        if(reconnect) {
            try {
                if(mSocket != null) {
                    mSocket.close();
                }
            } catch(IOException e) {
            }
            setFastPolling(true);
        }
        return reconnect;
    }

    @Override
    public boolean isConnected() {
        boolean connected = false;
        // If we can't get the lock in 100ms, must be blocked waiting for a
        // connection so we consider it disconnected.
        try {
            if(mConnectionLock.readLock().tryLock(100, TimeUnit.MILLISECONDS)) {
                connected = super.isConnected();
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
            }
        } catch(InterruptedException e) { }

        return connected;
    }

    @Override
    public synchronized void stop() {
        if(isRunning()) {
            try {
                getContext().unregisterReceiver(mBroadcastReceiver);
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
        return MoreObjects.toStringHelper(this)
            .add("explicitDeviceAddress", mExplicitAddress)
            .add("connectedDeviceAddress", mConnectedAddress)
            .add("socket", mSocket)
            .toString();
    }

    private class SocketAccepter implements Runnable {
        private BluetoothServerSocket mmServerSocket;

        @Override
        public void run() {
            Log.d(TAG, "Socket accepter starting up");
            BluetoothSocket socket = null;
            while(isRunning() && shouldAttemptConnection()) {
                while(isConnected()) {
                    mConnectionLock.writeLock().lock();
                    try {
                        mDeviceChanged.await();
                    } catch(InterruptedException e) {

                    } finally {
                        mConnectionLock.writeLock().unlock();
                    }
                }

                // If the BT vehicle interface has been disabled, the socket
                // wlil be disconnected and we will break out of the above
                // while(isConnected()) loop and land here - double check that
                // this interface should still be running before trying to make
                // another connection.
                if(!isRunning()) {
                    break;
                }

                Log.d(TAG, "Initializing listening socket");
                mmServerSocket = mDeviceManager.listen();

                if(mmServerSocket == null) {
                    Log.i(TAG, "Unable to listen for Bluetooth connections " +
                            "- adapter may be off");
                    stopWhileBluetoothDisabled();
                    break;
                }

                try {
                    Log.i(TAG, "Listening for inbound socket connections");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                }

                if(socket != null) {
                    Log.i(TAG, "New inbound socket connection accepted");
                    manageConnectedSocket(socket);
                    try {
                        Log.d(TAG, "Closing listening server socket");
                        mmServerSocket.close();
                    } catch (IOException e) { }
                }
            }
            Log.d(TAG, "SocketAccepter is stopping");
            closeSocket();
            stop();
        }

        public void stop() {
            try {
                if(mmServerSocket != null)  {
                    mmServerSocket.close();
                }
            } catch (IOException e) { }
        }
    }
    @Override
    protected void connect() {
        if(!mUsePolling || !isRunning()) {
            return;
        }

        Log.i(TAG, "Beginning polling for Bluetooth devices");

        BluetoothDevice lastConnectedDevice =
                mDeviceManager.getLastConnectedDevice();

        BluetoothSocket newSocket = null;
        if(mExplicitAddress != null || !mPerformAutomaticScan) {
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
            mPerformAutomaticScan = false;
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

        manageConnectedSocket(newSocket);
    }

    private synchronized void manageConnectedSocket(BluetoothSocket socket) {
        mConnectionLock.writeLock().lock();
        try {
            mSocket = socket;
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

    @Override
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

    protected boolean write(byte[] bytes) {
        mConnectionLock.readLock().lock();
        boolean success = false;
        try {
            if(isConnected()) {
                mOutStream.write(new String(bytes));
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

    @Override
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

    @Override
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

    private boolean shouldAttemptConnection() {
        return mSocketAccepterRunning;
    }

    private void stopWhileBluetoothDisabled() {
        mSocketAccepterRunning = false;
        stopConnectionAttempts();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) ||
                    action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                // Whenever discovery finishes or another Bluetooth device connects
                // (i.e. it might be a car's infotainment system), take the
                // opportunity to try and connect to detected devices if we're not
                // already connected. Discovery may have been initiated by the
                // Enabler UI, or by some other user action or app.
                if(mUsePolling && !isConnected() && !mDeviceManager.isConnecting()) {
                    Log.d(TAG, "Discovery finished or a device connected, but " +
                            "we are not connected or attempting connections - " +
                            "kicking off reconnection attempts");
                    if(mExplicitAddress == null) {
                        mPerformAutomaticScan = true;
                    }
                    setFastPolling(true);
                }
            } else if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "Bluetooth adapter turned off");
                    stopWhileBluetoothDisabled();
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "Bluetooth adapter turned on");
                    mSocketAccepterRunning = true;
                    setFastPolling(true);
                    mAcceptThread = new Thread(new SocketAccepter());
                    mAcceptThread.start();
                    break;
                }
            }
        }
    };

}
