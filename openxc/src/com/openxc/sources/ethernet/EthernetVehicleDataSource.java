package com.openxc.sources.ethernet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

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
 * A vehicle data source reading measurements from an OpenXC Ethernet device.
 *
 * This class looks for a Ethernet device and expects to read OpenXC-compatible,
 * newline separated JSON messages in Ethernet frames.
 *
 */
public class EthernetVehicleDataSource extends ContextualVehicleDataSource
        implements Runnable, VehicleController {
    private static final String TAG = "EthernetVehicleDataSource";
    private static final int SOCKET_TIMEOUT = 10000;
    private static final int FRAME_LENGTH = 128;

    private boolean mRunning;
    private Socket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private SocketAddress mAddress = null;

    /**
     * Construct an instance of EthernetVehicleDataSource with a receiver
     * callback and custom device URI.
     *
     * If the device cannot be found at initialization, the object will block
     * waiting for a signal to check again.
     *
     *
     * @param context
     *            The Activity or Service context, used to get access to the
     *            Android EthernetManager.
     * @param callback
     *            An object implementing the SourceCallback that should receive
     *            data as it is received and parsed.
     * @param device
     *            a Ethernet device URI (see {@link EthernetDeviceUtilities} for
     *            the format) to look for.
     * @throws DataSourceException
     *             If no connection could be established
     */
    public EthernetVehicleDataSource(InetSocketAddress address,
            SourceCallback callback, Context context) throws DataSourceException {
        super(callback, context);

        if(address == null) {
            throw new EthernetDeviceException("Invalid address: " + address);
        }
        mAddress = address;
        start();
    }

    public EthernetVehicleDataSource(String address, SourceCallback callback,
            Context context) throws DataSourceException {
        this(socketAddressFromString(address), callback, context);
    }

    private static InetSocketAddress socketAddressFromString(String address)
            throws DataSourceException {
        String addressSplit[] = address.split(":");
        if(addressSplit.length != 2) {
            throw new DataSourceException(
                "Device address in wrong format -- expected: ip:port");
        }

        // TODO do we handle addresses without a port? 80 by default?
        Integer port;
        try {
            port = Integer.valueOf(addressSplit[1]);
        } catch(NumberFormatException e) {
            throw new DataSourceException(
                "Port \"" + addressSplit[0] + "\" is not a valid integer");
        }
        return new InetSocketAddress(addressSplit[0], port);
    }

    public EthernetVehicleDataSource(InetSocketAddress address, Context context)
            throws DataSourceException {
        this(address, null, context);
    }

    public EthernetVehicleDataSource(String address, Context context)
            throws DataSourceException {
        this(address, null, context);
    }

    /**
     * Opens an input stream as well as an output stream on the given socket. On
     * success the running flag will be set to true and the thread will be
     * launched.
     */
    public synchronized void start() {
        if(!mRunning) {
            mRunning = true;
            new Thread(this).start();
        }
    }

    /**
     * Quits the running connections and closes the ethernet socket.
     *
     * This should be called before the object is given up to the garbage
     * collector to avoid leaking a receiver in the Android framework.
     */
    public void stop() {
        super.stop();
        Log.d(TAG, "Stopping ethernet listener");

        if(mSocket != null) {
            try {
                mSocket.close();
            } catch (Exception e) {
                Log.w(TAG, "Couldn't close socket. Quit.");
            }
        }

        if(!mRunning) {
            Log.d(TAG, "Already stopped.");
        }
        else {
            mRunning = false;
        }
    }

    public static boolean validateAddress(String address) {
        try {
            socketAddressFromString(address);
        } catch(DataSourceException e) {
            return false;
        }
        return true;
    }

    private void connectStreams() throws EthernetDeviceException {
        try {
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();
        } catch(IOException e) {
            String message = "Error opening Ethernet socket streams";
            Log.e(TAG, message, e);
            disconnected();
            throw new EthernetDeviceException(message);
        }
        Log.i(TAG, "Socket created, streams assigned");
    }

    protected String getTag() {
        return TAG;
    }

    protected void disconnect() {
        if(mSocket == null) {
            Log.w(TAG, "Unable to disconnect -- not connected");
            return;
        }

        Log.d(TAG, "Disconnecting from the socket " + mSocket);
        try {
            if(mOutStream != null) {
                mOutStream.close();
            }

            if(mInStream != null) {
                mInStream.close();
            }
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

    public void run() {
        byte[] frame = new byte[FRAME_LENGTH];

        BytestreamDataSourceMixin buffer = new BytestreamDataSourceMixin();
        while(mRunning) {
            try {
                waitForDeviceConnection();
            } catch(EthernetDeviceException e) {
                Log.i(TAG, "Unable to connect to target IP address -- " +
                        "sleeping for awhile before trying again");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e2){
                    stop();
                }
                continue;
            }

            int received = 0;
            try {
                mInStream.read(frame, 0, FRAME_LENGTH);
            } catch(IOException e) {
                Log.e(TAG, "Unable to read response");
                disconnect();
                continue;
            }

            if(received == -1) {
                Log.w(TAG, "Lost connection to Ethernet stream");
                break;
            }

            if(received > 0) {
                buffer.receive(frame, received);
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("address", mAddress)
            .add("socket", mSocket)
            .toString();
    }

    public void set(RawMeasurement command) {
        String message = command.serialize() + "\u0000";
        Log.d(TAG, "Writing message to Ethernet: " + message);
        byte[] bytes = message.getBytes();
        write(bytes);
    }

    private void waitForDeviceConnection() throws EthernetDeviceException {
        if(mSocket == null) {
            mSocket = new Socket();
            try {
                mSocket.connect(mAddress, SOCKET_TIMEOUT);
            } catch(IOException e) {
                String message = "Error opening streams";
                Log.e(TAG, message, e);
                disconnect();
                throw new EthernetDeviceException(message, e);
            }

            if(!mSocket.isConnected()) {
                disconnect();
                throw new EthernetDeviceException("Could not connect to server!");
            }

            connected();
            connectStreams();
        }
    }

    /**
     * Writes given data to the socket.
     *
     * @param bytes
     *            will be written to the socket
     */
    private void write(byte[] bytes) {
        if(mSocket != null && mSocket.isConnected()) {
            Log.d(TAG, "Writing bytes to socket: " + bytes);
            try {
                mOutStream.write(bytes);
            } catch (Exception e) {
                Log.w(TAG, "Unable to write CAN message to Ethernet. Error: " + e.toString());
            }
        } else {
            Log.w(TAG, "No connection established, could not send anything.");
        }
    }
}
