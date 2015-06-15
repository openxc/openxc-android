package com.openxc.sources;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.util.Log;

import com.openxc.messages.SerializationException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.streamers.BinaryStreamer;
import com.openxc.messages.streamers.JsonStreamer;
import com.openxc.messages.streamers.VehicleMessageStreamer;
import com.openxc.sinks.DataSinkException;

/**
 * Common functionality for data sources that read a stream of newline-separated
 * messages in a separate thread from the main activity.
 */
public abstract class BytestreamDataSource extends ContextualVehicleDataSource
        implements Runnable {
    private final static int READ_BATCH_SIZE = 512;
    private static final int MAX_FAST_RECONNECTION_ATTEMPTS = 6;
    protected static final int RECONNECTION_ATTEMPT_WAIT_TIME_S = 10;
    protected static final int SLOW_RECONNECTION_ATTEMPT_WAIT_TIME_S = 60;

    private AtomicBoolean mRunning = new AtomicBoolean(false);
    private int mReconnectionAttempts;
    protected final ReadWriteLock mConnectionLock = new ReentrantReadWriteLock();
    protected final Condition mDeviceChanged = mConnectionLock.writeLock().newCondition();
    private Thread mThread;
    private Timer mTimer;
    private BytestreamConnectingTask mConnectionCheckTask;
    private VehicleMessageStreamer mStreamHandler = null;
    private boolean mFastPolling = true;

    public BytestreamDataSource(SourceCallback callback, Context context) {
        super(callback, context);
    }

    public void start() {
        if(mRunning.compareAndSet(false, true)) {
            Log.d(getTag(), "Starting " + getTag() + " source");
            mThread = new Thread(this);
            mThread.start();
        }
    }

    @Override
    public void stop() {
        if(mRunning.compareAndSet(true, false)) {
            Log.d(getTag(), "Stopping " + getTag() + " source");
            mThread.interrupt();
        }
    }

    protected void setFastPolling(boolean enabled) {
        mReconnectionAttempts = 0;
        if(enabled) {
            resetConnectionAttempts(0, RECONNECTION_ATTEMPT_WAIT_TIME_S);
        } else if(mFastPolling) {
            resetConnectionAttempts(SLOW_RECONNECTION_ATTEMPT_WAIT_TIME_S,
                    SLOW_RECONNECTION_ATTEMPT_WAIT_TIME_S);
        }
        mFastPolling = enabled;
    }

    /**
     * If not already connected to the data source, initiate the connection and
     * block until ready to be read.
     *
     * You must have the mConnectionLock locked before calling this
     * function.
     *
     * @throws InterruptedException if the interrupted while blocked -- probably
     *      shutting down.
     */
    protected void waitForConnection() throws InterruptedException {
        if(!isConnected() && mConnectionCheckTask == null) {
            setFastPolling(true);
        }

        while(isRunning() && !isConnected()) {
            ++mReconnectionAttempts;
            mConnectionLock.writeLock().lock();
            try {
                mDeviceChanged.await();

                if(mReconnectionAttempts == MAX_FAST_RECONNECTION_ATTEMPTS) {
                    Log.d(this.getClass().getSimpleName(),
                            "Unable to connect after " +
                            MAX_FAST_RECONNECTION_ATTEMPTS +
                            " attempts, slowing down attempts to every " +
                            SLOW_RECONNECTION_ATTEMPT_WAIT_TIME_S + " seconds");
                    setFastPolling(false);
                }
            } finally {
                mConnectionLock.writeLock().unlock();
            }
        }

        mReconnectionAttempts = 0;
    }

    private void resetConnectionAttempts(long delay, long period) {
        stopConnectionAttempts();
        mConnectionCheckTask = new BytestreamConnectingTask(this);
        mTimer = new Timer();
        mTimer.schedule(mConnectionCheckTask, delay * 1000, period * 1000);
    }

    protected void stopConnectionAttempts() {
        if(mTimer != null) {
            mTimer.cancel();
        }
        mReconnectionAttempts = 0;
    }

    @Override
    public void run() {
        while(isRunning()) {
            try {
                waitForConnection();
            } catch(InterruptedException e) {
                Log.i(getTag(), "Interrupted while waiting for connection - stopping the source");
                stop();
                break;
            }

            int received;
            byte[] bytes = new byte[READ_BATCH_SIZE];
            try {
                received = read(bytes);
            } catch(IOException e) {
                Log.e(getTag(), "Unable to read response", e);
                disconnect();
                continue;
            }

            if(received == -1) {
                Log.e(getTag(), "Error on read - returned -1");
                disconnect();
                continue;
            }

            if(received > 0) {
                synchronized(this) {
                    if(mStreamHandler == null) {
                        if(JsonStreamer.containsJson(new String(bytes))) {
                            mStreamHandler = new JsonStreamer();
                            Log.i(getTag(), "Source is sending JSON");
                        } else {
                            mStreamHandler = new BinaryStreamer();
                            Log.i(getTag(), "Source is sending protocol buffers");
                        }
                    }
                }

                mStreamHandler.receive(bytes, received);
                VehicleMessage message;
                while((message = mStreamHandler.parseNextMessage()) != null) {
                    handleMessage(message);
                }
            }
        }
        disconnect();
        Log.d(getTag(), "Stopped " + getTag());
    }

    public void receive(VehicleMessage command) throws DataSinkException {
        if(isConnected()) {
            VehicleMessageStreamer streamer;
            synchronized(this) {
                streamer = mStreamHandler;
                if(streamer == null) {
                    // See https://github.com/openxc/openxc-android/issues/181
                    streamer = new JsonStreamer();
                    Log.i(getTag(), "Payload format unknown, guessing JSON");
                }
            }

            try {
                if(!write(streamer.serializeForStream(command))) {
                    throw new DataSinkException("Unable to send command");
                }
            } catch(SerializationException e) {
                throw new DataSinkException(
                        "Unable to serialize command for sending", e);
            }
        } else {
            throw new DataSinkException("Not connected");
        }
    }

    @Override
    public boolean isConnected() {
        return isRunning();
    }

    /**
     * Must have the connection lock before calling this function
     */
    @Override
    protected void disconnected() {
        mDeviceChanged.signal();
        super.disconnected();
    }

    /**
     * Must have the connection lock before calling this function
     */
    @Override
    protected void connected() {
        mDeviceChanged.signal();
        super.connected();
    }

    /**
     * Returns true if this source should be running, or if it should die.
     *
     * This is different than isConnected - they just happen to return the same
     * thing in this base data source.
     */
    protected boolean isRunning() {
        return mRunning.get();
    }

    /**
     * Read data from the source into the given array.
     *
     * No more than bytes.length bytes will be read, and there is no guarantee
     * that any bytes will be read at all.
     *
     * @param bytes the destination array for bytes from the data source.
     * @return the number of bytes that were actually copied into bytes.
     * @throws IOException if the source is unexpectedly closed or returns an
     *      error.
     */
    protected abstract int read(byte[] bytes) throws IOException;

    protected abstract boolean write(byte[] bytes);

    /**
     * Perform any cleanup necessary to disconnect from the interface.
     */
    protected abstract void disconnect();

    /** Initiate a connection to the vehicle interface. */
    protected abstract void connect() throws DataSourceException;
}
