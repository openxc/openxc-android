package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.trace.TraceVehicleDataSource;

public class TraceSourcePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "TraceSourcePreferenceManager";

    private TraceVehicleDataSource mTraceSource;

    public TraceSourcePreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopTrace();
    }

    /**
     * Enable or disable receiving vehicle data from a Trace CAN device.
     *
     * @param enabled true if trace source should be enabled
     */
    private synchronized void setTraceSourceStatus(boolean enabled) {
        Log.i(TAG, "Setting trace data source to " + enabled);
        if(enabled) {
            String traceFile = getPreferenceString(
                    R.string.trace_source_file_key);
            if(traceFile != null ) {
                if(mTraceSource == null ||
                        !mTraceSource.sameFilename(traceFile)) {
                    stopTrace();

                    try {
                        mTraceSource = new TraceVehicleDataSource(
                                getContext(), traceFile);
                    } catch(DataSourceException e) {
                        Log.w(TAG, "Unable to add Trace source", e);
                        return;
                    }
                    getVehicleManager().addSource(mTraceSource);
                } else {
                    Log.d(TAG, "Trace file + " + traceFile + " already playing");
                }
            } else {
                Log.d(TAG, "No trace file set yet (" + traceFile +
                        "), not starting playback");
            }
        } else {
            stopTrace();
        }
    }

    private synchronized void stopTrace() {
        getVehicleManager().removeSource(mTraceSource);
        if(mTraceSource != null) {
            mTraceSource.stop();
            mTraceSource = null;
        }
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new TraceSourcePreferenceListener(preferences);
    }

    private class TraceSourcePreferenceListener extends PreferenceListener {

        public TraceSourcePreferenceListener(SharedPreferences preferences) {
            super(preferences);
        }

        public void readStoredPreferences() {
            setTraceSourceStatus(getPreferences().getBoolean(
                    getString(R.string.trace_source_checkbox_key), false));
        }

        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.trace_source_checkbox_key))
                    || key.equals(getString(R.string.trace_source_file_key))) {
                readStoredPreferences();
            }
        }
    }
}
