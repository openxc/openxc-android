package com.openxc.sources;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class BytestreamConnectingTask extends TimerTask {
    private static final int RECONNECTION_ATTEMPT_WAIT_TIME_S = 10;

    private BytestreamDataSource mSource;
    private Timer mTimer = new Timer();

    public BytestreamConnectingTask(BytestreamDataSource source) {
        mSource = source;
        mTimer.schedule(this, 0, RECONNECTION_ATTEMPT_WAIT_TIME_S * 1000);
    }

    public void run() {
        if(!mSource.isRunning() || mSource.isConnected()) {
            return;
        }

        try {
            mSource.connect();
        } catch(DataSourceException e) {
            Log.i(mSource.toString(), "Unable to connect to source, trying again in " +
                    RECONNECTION_ATTEMPT_WAIT_TIME_S + "s");
            Log.d(mSource.toString(), "Unable to connect because of exception", e);
        }

        if(mSource.isConnected()) {
            mTimer.cancel();
        }
    }
}
