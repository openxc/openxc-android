package com.openxc.sources;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.util.Log;

/**
 * Common functionality for data sources that read a stream of newline-separated
 * messages in a separate thread from the main activity.
 */
public abstract class BytestreamDataSource extends ContextualVehicleDataSource
        implements Runnable {
    // TODO could let subclasses override this
    private final static int READ_BATCH_SIZE = 512;
    private boolean mRunning = false;
    private final Lock mConnectionLock = new ReentrantLock();
    protected final Condition mDeviceChanged = mConnectionLock.newCondition();

    public BytestreamDataSource(SourceCallback callback, Context context) {
        super(callback, context);
    }

    public BytestreamDataSource(Context context) {
        this(null, context);
    }

    public synchronized void start() {
        if(!isRunning()) {
            mRunning = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        disconnect();
        super.stop();
        if(!isRunning()) {
            Log.d(getTag(), "Already stopped.");
            return;
        }
        Log.d(getTag(), "Stopping " + getTag() + " source");
        mRunning = false;
    }

    public void run() {
        BytestreamBuffer buffer = new BytestreamBuffer();
        while(isRunning()) {
            lockConnection();

            try {
                waitForConnection();
            } catch(DataSourceException e) {
                Log.i(getTag(), "Unable to connect to target device -- " +
                        "sleeping for awhile before trying again");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e2){
                    stop();
                }
                unlockConnection();
                continue;
            } catch(InterruptedException e) {
                stop();
                unlockConnection();
                continue;
            }

            if(!isRunning()) {
                break;
            }

            int received;
            byte[] bytes = new byte[READ_BATCH_SIZE];
            try {
                received = read(bytes);
            } catch(IOException e) {
                Log.e(getTag(), "Unable to read response", e);
                unlockConnection();
                disconnect();
                continue;
            }

            if(received > 0) {
                buffer.receive(bytes, received);
                for(String record : buffer.readLines()) {
                    handleMessage(record);
                }
            }

            unlockConnection();
        }
        Log.d(getTag(), "Stopped " + getTag());
    }

    @Override
    public synchronized boolean isConnected() {
        return mRunning;
    }

    /**
     * Returns true if this source should be running, or if it should die.
     *
     * This is different than isConnected - they just happen to return the same
     * thing in this base data source.
     */
    protected synchronized boolean isRunning() {
        return mRunning;
    }

    protected void lockConnection() {
        mConnectionLock.lock();
    }

    protected void unlockConnection() {
        mConnectionLock.unlock();
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

    /**
     * If not already connected to the data source, initiate the connection and
     * block until ready to be read.
     *
     * @throws DataSourceException The connection is still alive, but it
     *      returned an unexpected result that cannot be handled.
     * @throws InterruptedException if the interrupted while blocked -- probably
     *      shutting down.
     */
    protected abstract void waitForConnection() throws DataSourceException,
              InterruptedException;

    /**
     * Perform any cleanup necessary to disconnect from the interface.
     */
    protected abstract void disconnect();
};
