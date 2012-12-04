package com.openxc.sources;

import android.content.Context;

/**
 * A parent class for data sources that require access to an Android context.
 */
public abstract class ContextualVehicleDataSource extends BaseVehicleDataSource {
    private Context mContext;
    private WakeLockManager mWakeLocker;

    public ContextualVehicleDataSource(Context context) {
        this(null, context);
    }

    public ContextualVehicleDataSource(SourceCallback callback,
            Context context) {
        super(callback);
        mContext = context;
        mWakeLocker = new WakeLockManager(getContext(), getTag());
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * The data source is connected, so if necessary, keep the device awake.
     */
    protected void connected() {
        mWakeLocker.acquireWakeLock();
    }

    /**
     * The data source is connected, so if necessary, let the device go to
     * sleep.
     */
    protected void disconnected() {
        mWakeLocker.releaseWakeLock();
    }
}
