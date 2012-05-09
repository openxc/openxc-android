package com.openxc.sinks;

import java.util.Map;

import com.google.common.base.Objects;

import com.openxc.remote.RawMeasurement;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import android.util.Log;

public class RemoteCallbackSink extends AbstractQueuedCallbackSink {
    private final static String TAG = "RemoteCallbackSink";

    private int mListenerCount;
    private RemoteCallbackList<RemoteVehicleServiceListenerInterface>
            mListeners;

    public RemoteCallbackSink() {
        super();
        mListeners = new RemoteCallbackList<
            RemoteVehicleServiceListenerInterface>();
    }

    public synchronized void register(
            RemoteVehicleServiceListenerInterface listener) {
        mListeners.register(listener);
        ++mListenerCount;

        // send the last known value of all measurements to the new listener
        for(Map.Entry<String, RawMeasurement> entry :
                mMeasurements.entrySet()) {
            try {
                listener.receive(entry.getKey(), entry.getValue());
            } catch(RemoteException e) {
                Log.w(TAG, "Couldn't notify application " +
                        "listener -- did it crash?", e);
                break;
            }
        }
    }

    public void unregister(RemoteVehicleServiceListenerInterface listener) {
        mListeners.unregister(listener);
        --mListenerCount;
    }

    public int getListenerCount() {
        return mListenerCount;
    }


    protected void propagateMeasurement(String measurementId,
            RawMeasurement measurement) {
        int i = mListeners.beginBroadcast();
        while(i > 0) {
            i--;
            try {
                mListeners.getBroadcastItem(i).receive(measurementId,
                        measurement);
            } catch(RemoteException e) {
                Log.w(TAG, "Couldn't notify application " +
                        "listener -- did it crash?", e);
            }
        }
        mListeners.finishBroadcast();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("numListeners", getListenerCount())
            .toString();
    }
};
