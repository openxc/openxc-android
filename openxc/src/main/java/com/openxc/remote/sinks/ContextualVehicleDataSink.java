package com.openxc.remote.sinks;

import android.content.Context;

public class ContextualVehicleDataSink extends BaseVehicleDataSink {
    private Context mContext;

    public ContextualVehicleDataSink(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }
}
