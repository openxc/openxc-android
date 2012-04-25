package com.openxc.remote.sinks;

import android.content.Context;

public abstract class ContextualVehicleDataSink extends AbstractVehicleDataSink {
    private Context mContext;

    public ContextualVehicleDataSink(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }
}
