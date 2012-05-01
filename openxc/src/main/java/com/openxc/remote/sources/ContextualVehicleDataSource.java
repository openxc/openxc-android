package com.openxc.remote.sources;

import android.content.Context;

public class ContextualVehicleDataSource extends BaseVehicleDataSource {
    private Context mContext;

    public ContextualVehicleDataSource(Context context) {
        this(null, context);
    }

    public ContextualVehicleDataSource(SourceCallback callback,
            Context context) {
        super(callback);
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }
}
