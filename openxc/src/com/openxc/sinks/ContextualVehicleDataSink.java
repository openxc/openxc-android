package com.openxc.sinks;

import android.content.Context;

/**
 * A parent class for data sinks that require access to an Android context.
 */
public class ContextualVehicleDataSink extends BaseVehicleDataSink {
    private Context mContext;

    public ContextualVehicleDataSink(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }
}
