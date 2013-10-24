package com.openxc.interfaces.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.interfaces.UriBasedVehicleInterfaceMixin;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.remote.RawMeasurement;
import com.openxc.sources.BytestreamConnectingTask;
import com.openxc.sources.BytestreamDataSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;
import com.openxc.sources.SourceCallback;

/**
 * A vehicle data source reading measurements from an OpenXC network device.
 *
 * This class looks for a network device and expects to read OpenXC-compatible,
 * newline separated JSON messages.
 */
public class NetworkVehicleInterface extends BytestreamDataSource
        implements VehicleInterface {
    private static final String TAG = "NetworkVehicleInterface";
    private static final int SOCKET_TIMEOUT = 10000;
    private static final String SCHEMA_SPECIFIC_PREFIX = "//";

    private Socket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private URI mUri;

    /**
     * Construct an instance of NetworkVehicleInterface with a receiver
     * callback and custom device URI.
     *
     * If the device cannot be found at initialization, the object will block
     * waiting for a signal to check again.
     *
     *
     * @param context
     *            The Activity or Service context, used to get access to the
     *            Android NetworkManager.
     * @param callback
     *            An object implementing the SourceCallback that should receive
     *            data as it is received and parsed.
     * @param uri
     *            The network host's address.
     * @throws DataSourceException
     *             If no connection could be established
     */
    public NetworkVehicleInterface(SourceCallback callback, Context context,
            URI uri) throws DataSourceException {
        super(callback, context);
        setUri(uri);
        start();
    }

    public NetworkVehicleInterface(Context context, URI uri)
            throws DataSourceException {
        this(null, context, uri);
    }

    public NetworkVehicleInterface(Context context, String uriString)
            throws DataSourceException {
        this(context, UriBasedVehicleInterfaceMixin.createUri(
                    massageUri(uriString)));
    }

    public boolean setResource(String otherResource) throws DataSourceException {
        if(!UriBasedVehicleInterfaceMixin.sameResource(mUri,
                massageUri(otherResource))) {
            setUri(otherResource);
            try {
                if(mSocket != null) {
                    mSocket.close();
                }
            } catch(IOException e) {
            }
            return true;
        }
        return false;
    }

    /**
     * Return true if the address and port are valid.
     *
     * @return true if the address and port are valid.
     */
    public static boolean validateResource(String uriString) {
        return UriBasedVehicleInterfaceMixin.validateResource(
                massageUri(uriString));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("uri", mUri)
            .toString();
    }

    public boolean receive(RawMeasurement command) {
        String message = command.serialize() + "\u0000";
        byte[] bytes = message.getBytes();
        return write(bytes);
    }

    @Override
    public boolean isConnected() {
        lockConnection();
        boolean connected = mSocket != null && mSocket.isConnected() && super.isConnected();
        unlockConnection();
        return connected;
    }

    protected int read(byte[] bytes) throws IOException {
        lockConnection();
        int bytesRead = -1;
        try {
            if(isConnected()) {
                bytesRead = mInStream.read(bytes, 0, bytes.length);
            }
        } finally {
            unlockConnection();
        }
        return bytesRead;
    }

    protected void connect() throws NetworkSourceException {
        if(!isRunning()) {
            return;
        }

        lockConnection();
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(mUri.getHost(),
                        mUri.getPort()), SOCKET_TIMEOUT);
            if(!isConnected()) {
                Log.d(TAG, "Could not connect to server");
                disconnected();
            } else {
                connected();
                connectStreams();
            }
        } catch(IOException e) {
            String message = "Error opening streams";
            Log.e(TAG, message, e);
            disconnect();
            throw new NetworkSourceException(message, e);
        } catch(AssertionError e) {
            String message = "Error opening streams";
            Log.e(TAG, message, e);
            disconnect();
            throw new NetworkSourceException(message, e);
        } finally {
            unlockConnection();
        }
    }

    protected void disconnect() {
        if(!isConnected()) {
            return;
        }

        lockConnection();
        try {
            Log.d(TAG, "Disconnecting from the socket " + mSocket);
            try {
                if(mInStream != null) {
                    mInStream.close();
                    mInStream = null;
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the input stream", e);
            }

            try {
                if(mOutStream != null) {
                    mOutStream.close();
                    mOutStream = null;
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the output stream", e);
            }

            try {
                if(mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the socket", e);
            }
            disconnected();
        } finally {
            unlockConnection();
        }
        Log.d(TAG, "Disconnected from the socket");
    }

    /**
     * Writes given data to the socket.
     *
     * @param bytes data to write to the socket.
     * @return true if the data was written successfully.
     */
    private synchronized boolean write(byte[] bytes) {
        lockConnection();
        boolean success = true;
        try {
            if(isConnected()) {
                Log.d(TAG, "Writing bytes to socket: " + bytes);
                mOutStream.write(bytes);
            } else {
                Log.w(TAG, "No connection established, could not send anything.");
                success = false;
            }
        } catch(IOException e) {
            Log.w(TAG, "Unable to write CAN message to Network. Error: " + e.toString());
            success = false;
        } finally {
            unlockConnection();
        }
        return success;
    }

    protected String getTag() {
        return TAG;
    }

    /**
     * You must have call lockConnection before using this function - this
     * method is private so we're letting the caller handle it.
     */
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

    /**
     * Add the prefix reuqired to parse with URI if it's not already there.
     */
    private static String massageUri(String uriString) {
        if(!uriString.startsWith(SCHEMA_SPECIFIC_PREFIX)) {
            uriString = SCHEMA_SPECIFIC_PREFIX + uriString;
        }
        return uriString;
    }

    private void setUri(String uri) throws DataSourceException {
        setUri(UriBasedVehicleInterfaceMixin.createUri(massageUri(uri)));
    }

    private void setUri(URI uri) throws DataSourceResourceException {
        if(uri == null || !UriBasedVehicleInterfaceMixin.validateResource(uri)) {
            throw new DataSourceResourceException("URI is not valid");
        }

        mUri = uri;
    }
}
