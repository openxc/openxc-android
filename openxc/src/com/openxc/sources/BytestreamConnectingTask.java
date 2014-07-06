package com.openxc.sources;

import java.util.TimerTask;

import android.util.Log;

public class BytestreamConnectingTask extends TimerTask {
    private BytestreamDataSource mSource;

    public BytestreamConnectingTask(BytestreamDataSource source) {
        mSource = source;
    }

    @Override
    public void run() {
        if(!mSource.isRunning() || mSource.isConnected()) {
            return;
        }

        try {
            mSource.connect();
        } catch(DataSourceException e) {
            Log.d(mSource.toString(), "Unable to connect because of exception", e);
        }
    }
}
