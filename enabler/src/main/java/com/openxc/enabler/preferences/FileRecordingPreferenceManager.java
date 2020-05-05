package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
    @Override
    public void close() {
        super.close();
        stopRecording();
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListenerImpl(this);
    }

    public  void splitTraceFile() {

        stopRecording();
        String directory = getPreferenceString(R.string.recording_directory_key);
        if (directory != null && mFileRecorder == null) {
                mCurrentDirectory = directory;
                mFileRecorder = new FileRecorderSink(
                        new AndroidFileOpener(directory));
                getVehicleManager().addSink(mFileRecorder);

        }
    }
    private void setFileRecordingStatus(boolean enabled) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("IsTraceRecording", enabled);
        editor.commit();

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        boolean isTracePlaying = sharedpreferences.getBoolean("isTracePlayingEnabled", false);
        if(enabled && !isTracePlaying) {
            String directory = getPreferenceString(R.string.recording_directory_key);
            if(directory != null && mFileRecorder == null || !mCurrentDirectory.equals(directory)) {

                    mCurrentDirectory = directory;
                    stopRecording();

                    mFileRecorder = new FileRecorderSink(
                            new AndroidFileOpener(directory));
                    getVehicleManager().addSink(mFileRecorder);

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
        if (directory != null && mFileRecorder != null) {

                getVehicleManager().addSink(mFileRecorder);
        }
    }
    /**
     * Internal implementation of the {@link VehiclePreferenceManager.PreferenceListener}
     * interface.
     */
    private static final class PreferenceListenerImpl extends PreferenceListener {

        private static final int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.recording_checkbox_key
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
            final FileRecordingPreferenceManager reference
                    = (FileRecordingPreferenceManager) getEnclosingReference();
            if (reference == null) {
                Log.w(TAG, "Can not read stored preferences, enclosing instance is null");
                return;
            }

            reference.setFileRecordingStatus(reference.getPreferences().getBoolean(
                    reference.getString(R.string.recording_checkbox_key), false));
        }

        @Override
        protected int[] getWatchedPreferenceKeyIds() {
            return WATCHED_PREFERENCE_KEY_IDS;
        }
    }

}
