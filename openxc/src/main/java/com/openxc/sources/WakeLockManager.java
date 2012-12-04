package com.openxc.sources;

import android.content.Context;

import android.os.PowerManager;

import android.os.PowerManager.WakeLock;

import android.util.Log;

public class WakeLockManager {
    private Context mContext;
    private WakeLock mWakeLock;
    private String mTag;

    public WakeLockManager(Context context, String tag) {
        mTag = tag;
        mContext = context;
    }

    public void acquireWakeLock() {
        PowerManager manager = (PowerManager) mContext.getSystemService(
                Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mTag);
        mWakeLock.acquire();
        Log.d(mTag, "Wake lock acquired");
    }

    public void releaseWakeLock() {
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d(mTag, "Wake lock released");
        }
    }
}
