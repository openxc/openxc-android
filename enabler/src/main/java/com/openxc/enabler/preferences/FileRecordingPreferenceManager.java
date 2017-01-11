package com.openxc.enabler.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.Log;

import com.openxc.sinks.DataSinkException;
import com.openxc.sinks.FileRecorderSink;
import com.openxc.sinks.VehicleDataSink;
import com.openxc.util.AndroidFileOpener;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable recording of a trace file.
 */
public class FileRecordingPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "FileRecordingPreferenceManager";
    private VehicleDataSink mFileRecorder;
    private String mCurrentDirectory;

    public FileRecordingPreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopRecording();
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                    R.string.recording_checkbox_key,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setFileRecordingStatus(getPreferences().getBoolean(
                        getString(R.string.recording_checkbox_key), false));
            }
        };
    }


    private void setFileRecordingStatus(boolean enabled) {
        Log.i(TAG, "Setting recording to " + enabled);
        if(enabled) {
            String directory = getPreferenceString(R.string.recording_directory_key);
            if(directory != null) {
                if(mFileRecorder == null || !mCurrentDirectory.equals(directory)) {
                    mCurrentDirectory = directory;
                    stopRecording();

                    mFileRecorder = new FileRecorderSink(
                            new AndroidFileOpener(directory));
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
        if(getVehicleManager() != null){
            getVehicleManager().removeSink(mFileRecorder);
            mFileRecorder = null;
        }
    }
}
