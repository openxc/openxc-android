package com.openxc.sources.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.remote.RawMeasurement;
import com.openxc.sources.BytestreamDataSourceMixin;
import com.openxc.sources.ContextualVehicleDataSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

/**
 * A vehicle data source reading measurements from an OpenXC Network device.
 *
 * This class looks for a network device and expects to read OpenXC-compatible,
 * newline separated JSON messages in network frames.
 *
 */
public class NetworkVehicleDataSource extends ContextualVehicleDataSource
        implements Runnable, VehicleInterface {
    private static final String TAG = "NetworkVehicleDataSource";
    private static final int SOCKET_TIMEOUT = 10000;
    private static final int FRAME_LENGTH = 128;

    private boolean mRunning;
    private Socket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private String mStringAddress;
    private InetAddress mAddress;
    private int mPort;

    /**
     * Construct an instance of NetworkVehicleDataSource with a receiver
     * callback and custom device URI.
     *
     * If the device cannot be found at initialization, the object will block
     * waiting for a signal to check again.
     *
     *
     * @param address
     *            The network host address.
     * @param port
     *            The network host port.
     * @param context
     *            The Activity or Service context, used to get access to the
     *            Android NetworkManager.
     * @param callback
     *            An object implementing the SourceCallback that should receive
     *            data as it is received and parsed.
     * @throws DataSourceException
     *             If no connection could be established
     */
    public NetworkVehicleDataSource(String address, String port,
            SourceCallback callback, Context context)
            throws DataSourceException {
        super(callback, context);

        if(address == null) {
            throw new NetworkSourceException("Address cannot be null");
        }
        mStringAddress = address;
        mPort = createPort(port);
        start();
    }

    public NetworkVehicleDataSource(String address, String port, Context context)
            throws DataSourceException {
        this(address, port, null, context);
    }

    public synchronized void start() {
        if(!mRunning) {
            mRunning = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        super.stop();
        Log.d(TAG, "Stopping network listener");

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
    // TODO do we need to override close? yes i think, we need to close the
    // socket and such


    /**
     * Return true if the given address and port match those currently in use by
     * the network data source.
     *
     * @return true if the address and port match the current in-use values.
     */
    public boolean sameAddress(String address, String port) {
        try {
            return mAddress.equals(createAddress(address))
                && mPort == createPort(port);
        } catch(DataSourceException e) {
            return false;
        }
    }

    /**
     * Return true if the address and port are valid.
     *
     * @return true if the address and port are valid.
     */
    public static boolean validate(String address, String port) {
        return validatePort(port) && validateAddress(address);
    }

    /**
     * Returns true if the port is a valid network port.
     *
     * @return true if the port is a valid network port.
     */
    public static boolean validatePort(String portString) {
        try {
            Integer port = createPort(portString);
            return port > 0;
        } catch(DataSourceException e) {
            return false;
        }
    }

    /**
     * Return true if the address is a valid IP address or hostname.
     *
     * Basically if it's not null, it's valid right now.
     */
    public static boolean validateAddress(String address) {
        if(address == null) {
            Log.w(TAG, "Network host address not set (it's " + address + ")");
            return false;
        }
        return true;
    }

    public void run() {
        byte[] frame = new byte[FRAME_LENGTH];

        BytestreamDataSourceMixin buffer = new BytestreamDataSourceMixin();
        while(mRunning) {
            try {
                waitForDeviceConnection();
            } catch(DataSourceException e) {
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
                received = mInStream.read(frame, 0, FRAME_LENGTH);
            } catch(IOException e) {
                Log.e(TAG, "Unable to read response");
                disconnect();
                continue;
            }

            if(received == -1) {
                Log.w(TAG, "Lost connection to Network stream");
                break;
            }

            if(received > 0) {
                buffer.receive(frame, received);
                for(String record : buffer.readLines()) {
                    handleMessage(record);
                }
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

    public boolean receive(RawMeasurement command) {
        String message = command.serialize() + "\u0000";
        Log.d(TAG, "Writing message to network: " + message);
        byte[] bytes = message.getBytes();
        return write(bytes);
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

    private void waitForDeviceConnection() throws DataSourceException {
        if(mAddress == null) {
            mAddress = createAddress(mStringAddress);
        }

        if(mSocket == null) {
            mSocket = new Socket();
            try {
                mSocket.connect(new InetSocketAddress(mAddress, mPort),
                        SOCKET_TIMEOUT);
            } catch(IOException e) {
                String message = "Error opening streams";
                Log.e(TAG, message, e);
                disconnect();
                throw new NetworkSourceException(message, e);
            }

            if(!mSocket.isConnected()) {
                disconnect();
                throw new NetworkSourceException("Could not connect to server!");
            }

            connected();
            connectStreams();
        }
    }

    /**
     * Writes given data to the socket.
     *
     * @param bytes data to write to the socket.
     * @return true if the data was written successfully.
     */
    private synchronized boolean write(byte[] bytes) {
        if(mSocket != null && mSocket.isConnected()) {
            Log.d(TAG, "Writing bytes to socket: " + bytes);
            try {
                mOutStream.write(bytes);
            } catch(IOException e) {
                Log.w(TAG, "Unable to write CAN message to Network. Error: " + e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "No connection established, could not send anything.");
            return false;
        }
        return true;
    }

    private static int createPort(String port) throws DataSourceException {
        try {
            return Integer.valueOf(port);
        } catch(NumberFormatException e) {
            throw new DataSourceException(
                    "Target network port is invalid (" + port + ")", e);
        }
    }

    private static InetAddress createAddress(String address)
            throws DataSourceException {
        try {
            return InetAddress.getByName(address);
        } catch(UnknownHostException e) {
            throw new DataSourceException(
                    "Target network host is unreachable", e);
        }
    }

    private void connectStreams() throws NetworkSourceException {
        try {
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();
        } catch(IOException e) {
            String message = "Error opening Network socket streams";
            Log.e(TAG, message, e);
            disconnected();
            throw new NetworkSourceException(message);
        }
        Log.i(TAG, "Socket created, streams assigned");
    }
}
