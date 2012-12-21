package com.openxc.sources;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.util.Log;

public abstract class BytestreamDataSource extends ContextualVehicleDataSource implements Runnable {
    // TODO could let subclasses override this
    private final static int READ_BATCH_SIZE = 128;
    private boolean mRunning = false;
    private final Lock mConnectionLock;

    public BytestreamDataSource(SourceCallback callback, Context context) {
        super(callback, context);
        mConnectionLock = new ReentrantLock();
    }

    public BytestreamDataSource(Context context) {
        this(null, context);
    }

    public synchronized void start() {
        if(!mRunning) {
            mRunning = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        super.stop();
        if(!mRunning) {
            Log.d(getTag(), "Already stopped.");
            return;
        }
        Log.d(getTag(), "Stopping " + getTag() + " source");
        disconnect();
        mRunning = false;
    }

    public void run() {
        BytestreamBuffer buffer = new BytestreamBuffer();
        while(mRunning) {
            mConnectionLock.lock();

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
                continue;
            } catch(InterruptedException e) {
                stop();
                continue;
            }

            int received;
            byte[] bytes = new byte[READ_BATCH_SIZE];
            try {
                received = read(bytes);
            } catch(IOException e) {
                Log.e(getTag(), "Unable to read response");
                mConnectionLock.unlock();
                disconnect();
                continue;
            }

            if(received > 0) {
                buffer.receive(bytes, received);
                for(String record : buffer.readLines()) {
                    handleMessage(record);
                }
            }

            mConnectionLock.unlock();
        }
        Log.d(getTag(), "Stopped " + getTag());
    }

    protected boolean isRunning() {
        return mRunning;
    }

    protected void lockConnection() {
        mConnectionLock.lock();
    }

    protected void unlockConnection() {
        mConnectionLock.unlock();
    }

    protected Condition createCondition() {
        return mConnectionLock.newCondition();
    }

    protected abstract int read(byte[] bytes) throws IOException;
    protected abstract void waitForConnection() throws DataSourceException, InterruptedException;

    protected abstract void disconnect();
};
