package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openxc.enabler.R;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sinks.DataSinkException;
import com.openxc.sinks.FileRecorderSink;
import com.openxc.sinks.VehicleDataSink;
import com.openxc.util.AndroidFileOpener;

public class FileRecordingPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "FileRecordingPreferenceManager";
    private VehicleDataSink mFileRecorder;
    private String mCurrentDirectory;

    public FileRecordingPreferenceManager(Context context) {
        super(context);
    }

    /**
     * Enable or disable recording of a trace file.
     *
     * @param enabled true if recording should be enabled
     * @throws VehicleServiceException if the listener is unable to be
     *      unregistered with the library internals - an exceptional
     *      situation that shouldn't occur.
     */
    private void setFileRecordingStatus(boolean enabled)
            throws VehicleServiceException {
        Log.i(TAG, "Setting recording to " + enabled);
        if(enabled) {
            String directory = getPreferenceString(R.string.recording_directory_key);
            if(directory != null) {
                if(mFileRecorder == null || !mCurrentDirectory.equals(directory)) {
                    mCurrentDirectory = directory;
                    stopRecording();

                    try {
                        mFileRecorder = new FileRecorderSink(
                                new AndroidFileOpener(getContext(), directory));
                    } catch(DataSinkException e) {
                        Log.w(TAG, "Unable to start trace recording", e);
                    }
                    getVehicleManager().addSink(mFileRecorder);
                }
            } else {
                Log.d(TAG, "No recording base directory set (" + directory +
                        "), not starting recorder");
            }
        } else {
            stopRecording();
        }
    }

    private void stopRecording() {
        getVehicleManager().removeSink(mFileRecorder);
        mFileRecorder = null;
    }

    public void close() {
        super.close();
        stopRecording();
    }

    protected PreferenceListener createPreferenceListener(
            SharedPreferences preferences) {
        return new FileRecordingPreferenceListener(preferences);
    }

    private class FileRecordingPreferenceListener extends PreferenceListener {

        public FileRecordingPreferenceListener(SharedPreferences preferences) {
            super(preferences);
        }

        public void readStoredPreferences() {
            onSharedPreferenceChanged(mPreferences,
                        getString(R.string.recording_checkbox_key));
        }

        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.recording_checkbox_key))) {
                try {
                    setFileRecordingStatus(preferences.getBoolean(key, false));
                } catch(VehicleServiceException e) {
                    Log.w(TAG, "Unable to update vehicle service when preference \""
                            + key + "\" changed", e);
                }
            }
        }
    }
}
