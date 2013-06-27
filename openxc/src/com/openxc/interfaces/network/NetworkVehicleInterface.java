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
            stop();
            start();
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        disconnect();
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

    protected int read(byte[] bytes) throws IOException {
        return mInStream.read(bytes, 0, bytes.length);
    }

    protected String getTag() {
        return TAG;
    }

    protected void disconnect() {
        if(mSocket == null) {
            return;
        }

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

        disconnected();
        Log.d(TAG, "Disconnected from the socket");
    }

    protected void waitForConnection() throws DataSourceException {
        if(mSocket == null) {
            mSocket = new Socket();
            try {
                mSocket.connect(new InetSocketAddress(mUri.getHost(),
                            mUri.getPort()), SOCKET_TIMEOUT);
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
