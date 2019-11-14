package com.openxc.enabler;

import android.app.Application;

import com.openxc.sources.trace.TraceVehicleDataSource;

public class OpenXCApplication extends Application {

    private static TraceVehicleDataSource mTraceSource = null;


    @Override
    public void onCreate() {
        super.onCreate();
    }


    public static TraceVehicleDataSource getTraceSource() {
        return mTraceSource;
    }

    public static void setTraceSource(TraceVehicleDataSource traceVehicleDataSource) {
        mTraceSource = traceVehicleDataSource;
    }

}
