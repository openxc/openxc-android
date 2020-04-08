package com.openxc.enabler.preferences;

import android.content.Context;
import android.util.Log;

import com.openxc.enabler.OpenXCApplication;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.trace.TraceVehicleDataSource;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable receiving vehicle data from a pre-recorded trace file.
 */
public class TraceSourcePreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "TraceSourcePreferenceManager";

    private TraceVehicleDataSource mTraceSource;

    public TraceSourcePreferenceManager(Context context) {
        super(context);
    }
    @Override
    public void close() {
        super.close();
        stopTrace();
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListenerImpl(this);
    }

    private synchronized void setTraceSourceStatus(boolean enabled) {
        Log.i(TAG, "Setting trace data source to " + enabled);
        if(enabled) {
            try {
                getVehicleManager().setVehicleInterface(null);
            } catch(VehicleServiceException e) {
                Log.e(TAG, "Unable to remove existing vehicle interface");
            }

            String traceFile = getPreferenceString(
                    R.string.trace_source_file_key);
            if(traceFile != null ) {
                if(mTraceSource == null ||
                        !mTraceSource.sameFilename(traceFile)) {
                    addTraceDetails(traceFile);
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

    private void addTraceDetails(String traceFile) {
        stopTrace();

        try {
            mTraceSource = new TraceVehicleDataSource(
                    getContext(), traceFile);
        } catch(DataSourceException e) {
            Log.w(TAG, "Unable to add Trace source", e);
            return;
        }

        OpenXCApplication.setTraceSource(mTraceSource);
        getVehicleManager().addSource(mTraceSource);
    }

    private synchronized void stopTrace() {
        if(getVehicleManager() != null && mTraceSource != null){
            getVehicleManager().removeSource(mTraceSource);
            mTraceSource = null;
        }
    }

    /**
     * Internal implementation of the {@link VehiclePreferenceManager.PreferenceListener}
     * interface.
     */
    private static final class PreferenceListenerImpl extends PreferenceListener {

        private final static int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.vehicle_interface_key,
                R.string.trace_source_file_key
        };

        /**
         * Main constructor.
         *
         * @param reference Reference to the enclosing class.
         */
        private PreferenceListenerImpl(final VehiclePreferenceManager reference) {
            super(reference);
        }

        @Override
        protected void readStoredPreferences() {
            final TraceSourcePreferenceManager reference = (TraceSourcePreferenceManager) getEnclosingReference();
            if (reference == null) {
                Log.w(TAG, "Can not read stored preferences, enclosing instance is null");
                return;
            }

            reference.setTraceSourceStatus(reference.getPreferences().getString(
                    reference.getString(R.string.vehicle_interface_key), "").equals(
                    reference.getString(R.string.trace_interface_option_value)));
        }

        @Override
        protected int[] getWatchedPreferenceKeyIds() {
            return WATCHED_PREFERENCE_KEY_IDS;
        }
    }
}
