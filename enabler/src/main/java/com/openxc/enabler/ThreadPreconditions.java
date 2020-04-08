package com.openxc.enabler;

import android.os.Looper;

import com.openxcplatform.enabler.BuildConfig;

public class ThreadPreconditions {
    private ThreadPreconditions(){

    }
    public static void checkOnMainThread() {
        if (BuildConfig.DEBUG && Thread.currentThread() != Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("This method should be called from the Main Thread");
        }
    }
}

