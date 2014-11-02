package com.openxc.sinks;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleServiceListener;

/**
 * A data sink that sends new messages through an AIDL interface.
 *
 * This sink is used to send all new messages over an AIDL interface in
 * Android to applications using {@link com.openxc.VehicleManager}. Once
 * registered, a receiver gets all messages regardless of their type or
 * value.
 */
public class RemoteCallbackSink extends AbstractQueuedCallbackSink {
    private final static String TAG = "RemoteCallbackSink";

    private int mListenerCount;
    private RemoteCallbackList<VehicleServiceListener> mListeners =
            new RemoteCallbackList<VehicleServiceListener>();

    public synchronized void register(VehicleServiceListener listener) {
        synchronized(mListeners) {
            if(mListeners.register(listener)) {
                ++mListenerCount;
            }
        }
    }

    public void unregister(VehicleServiceListener listener) {
        synchronized(mListeners) {
            if(mListeners.unregister(listener)) {
                --mListenerCount;
            }
        }
    }

    public int getListenerCount() {
        return mListenerCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("numListeners", getListenerCount())
            .toString();
    }

    @Override
    protected void propagateMessage(VehicleMessage message) {
        synchronized(mListeners) {
            int i = mListeners.beginBroadcast();
            while(i > 0) {
                i--;
                try {
                    mListeners.getBroadcastItem(i).receive(message);
                } catch(RemoteException e) {
                    Log.w(TAG, "Couldn't notify application " +
                            "listener -- did it crash?", e);
                }
            }
            mListeners.finishBroadcast();
        }
    }
};
