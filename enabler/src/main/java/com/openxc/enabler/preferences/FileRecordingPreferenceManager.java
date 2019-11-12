package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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

    public  void splitTraceFile(boolean enable) {

        // setFileRecordingStatus(enable);

        stopRecording();
        String directory = getPreferenceString(R.string.recording_directory_key);
        if (directory != null) {
            if (mFileRecorder == null) {
                mCurrentDirectory = directory;
                //stopRecording();

                mFileRecorder = new FileRecorderSink(
                        new AndroidFileOpener(directory));
                getVehicleManager().addSink(mFileRecorder);
            }
        }
    }
    private void setFileRecordingStatus(boolean enabled) {

        Log.e(TAG, "splitTraceFile: this called");
        //Log.i(TAG, "Setting recording to " + enabled);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("IsTraceRecording", enabled);
        editor.commit();

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        boolean isTracePlaying = sharedpreferences.getBoolean("isTracePlayingEnabled", false);
        //Log.d(TAG, "Tracefile checklist recordvalue1:" + isTracePlaying);
        if(enabled && !isTracePlaying) {
            //Log.i(TAG, "Setting recording to " + enabled);
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
               // Log.d(TAG, "No recording base directory set (" + directory +"), not starting recorder");
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
    public void stopTraceRecording() {
        stopRecording();
    }
    public void startTraceRecording() {
        String directory = getPreferenceString(R.string.recording_directory_key);
        if (directory != null) {
            if (mFileRecorder != null) {
            getVehicleManager().addSink(mFileRecorder);
            //mFileRecorder = new FileRecorderSink(new AndroidFileOpener(directory));
            //getVehicleManager().addSink(mFileRecorder);
        }
    }
    }

}
