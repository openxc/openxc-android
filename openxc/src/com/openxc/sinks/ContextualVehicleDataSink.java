package com.openxc.sinks;

import android.content.Context;

import com.openxc.messages.VehicleMessage;

/**
 * A parent class for data sinks that require access to an Android context.
 */
public abstract class ContextualVehicleDataSink implements VehicleDataSink {
    private Context mContext;

    public ContextualVehicleDataSink(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public abstract void stop();

    @Override
    public abstract void receive(VehicleMessage message);
}
