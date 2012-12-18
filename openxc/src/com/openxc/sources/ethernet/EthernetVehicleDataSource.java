package com.openxc.sources.ethernet;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.openxc.controllers.VehicleController;
import com.openxc.remote.RawMeasurement;
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

    private boolean mRunning;

    private double mBytesReceived;

    private Socket nsocket; // Network Socket
    private InputStream nis; // Network Input Stream
    private OutputStream nos; // Network Output Stream

    SocketAddress sockaddr = null;
    private static final int SOCKET_TIMEOUT = 10000;
    private static final int FRAME_LENGTH = 128;

    private NetworkTask task;

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
    public EthernetVehicleDataSource(InetSocketAddress addr,
            SourceCallback callback, Context context) throws DataSourceException {
        super(callback, context);

        try {
            if(addr != null) {
                sockaddr = addr;
            } else {
                throw new Exception("Invalid InetSocketAddress object!");
            }

            task = new NetworkTask();
        } catch (Exception e) {
            String message = "Connection error: " + e.toString();
            Log.w(TAG, message);
            throw new DataSourceException(message);
        }
    }

    public EthernetVehicleDataSource(InetSocketAddress addr, Context context) throws DataSourceException {
        this(addr, null, context);
    }

    /**
     * Opens an input stream as well as an output stream on the given socket. On
     * success the running flag will be set to true and the thread will be
     * launched.
     */
    public synchronized void start() {
        if(!mRunning) {
            try {
                mRunning = true;
                run();
            } catch (Exception e) {
                Log.e(TAG, "Could not start EthernetVehicleDataSource!");
            }
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

        if(nsocket != null) {
            try {
                nsocket.close();
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

    class NetworkTask extends AsyncTask<Object,Object,Boolean> {

        /**
         * Continuously read JSON messages from an socket, or wait for incoming
         * data.
         *
         * This loop will only exit if {@link #stop()} is called - otherwise it
         * either waits for a new device connection or reads Ethernet packets.
         */
        @Override
        protected Boolean doInBackground(Object... arg0) {
            try {
                nsocket = new Socket();
                nsocket.connect(sockaddr, SOCKET_TIMEOUT);

                if(nsocket.isConnected()) {
                    start();
                } else {
                    throw new ConnectException("Could not connect to server!");
                }

                nis = nsocket.getInputStream();
                nos = nsocket.getOutputStream();
                Log.i(TAG, "Socket created, streams assigned");
                Log.i(TAG, "Waiting for initial data...");


                byte[] frame = new byte[FRAME_LENGTH];
                int read = 0;

                double lastLoggedTransferStatsAtByte = 0;
                StringBuffer buffer = new StringBuffer();
                final long startTime = System.nanoTime();
                long endTime;
                String line = new String();

                while (mRunning) {
                    try {
                        read = nis.read(frame, 0, FRAME_LENGTH); // This is blocking
                        while (read != -1) {
                            if(read > 0) {
                                // Creating a new String object for each message
                                // causes the GC to go a little crazy, but I
                                // don't see another obvious way of converting
                                // the byte[] to something the StringBuffer can
                                // accept (either char[] or String). See #151.
                                buffer.append(new String(frame, 0, read));

                                parseStringBuffer(buffer);
                                mBytesReceived += read;
                            }
                            read = nis.read(frame, 0, FRAME_LENGTH); // This is blocking
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
                endTime = System.nanoTime();

                if(mBytesReceived > lastLoggedTransferStatsAtByte + 1024 * 1024) {
                    lastLoggedTransferStatsAtByte = mBytesReceived;
                    logTransferStats(startTime, endTime);
                }

                return true;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    };

    public void run() {
        task.execute();
    }

    @Override
    public String toString() {
        if(nsocket != null ) {
            return nsocket.toString();
        } else {
            return "";
        }
    }

    public void set(RawMeasurement command) {
        String message = command.serialize() + "\u0000";
        Log.d(TAG, "Writing message to Ethernet: " + message);
        byte[] bytes = message.getBytes();
        write(bytes);
    }

    /**
     * Writes given data to the socket.
     *
     * @param bytes
     *            will be written to the socket
     */
    private void write(byte[] bytes) {
        if(nsocket != null && nsocket.isConnected()) {
            Log.d(TAG, "Writing bytes to socket: " + bytes);
            try {
                nos.write(bytes);
            } catch (Exception e) {
                Log.w(TAG, "Unable to write CAN message to Ethernet. Error: " + e.toString());
            }
        } else {
            Log.w(TAG, "No connection established, could not send anything.");
        }
    }

    private void logTransferStats(final long startTime, final long endTime) {
        double kilobytesTransferred = mBytesReceived / 1000.0;
        long elapsedTime = TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        Log.i(TAG, "Transferred " + kilobytesTransferred + " KB in " + elapsedTime + " seconds at an average of "
                + kilobytesTransferred / elapsedTime + " KB/s");
    }

    /**
     * Parses received data and passes the first line to the openxc
     *
     * @param buffer
     *            contains received messages
     */
    private void parseStringBuffer(StringBuffer buffer) {
        int newlineIndex = buffer.indexOf("\n");// search for the end of the
                                                // first line
        if(newlineIndex != -1) {
            final String messageString = buffer.substring(0, newlineIndex);
            buffer.delete(0, newlineIndex + 1);
            handleMessage(messageString);// pass a whole json-line to the
                                            // openxc-system
        }
    }
}
