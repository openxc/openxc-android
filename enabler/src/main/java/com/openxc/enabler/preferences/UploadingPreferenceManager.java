package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.openxc.sinks.DataSinkException;
import com.openxc.sinks.UploaderSink;
import com.openxc.sinks.VehicleDataSink;
import com.openxcplatform.enabler.R;

/**
 * Enable or disable uploading of a vehicle trace to a remote web server.
 *
 * The URL of the web server to upload the trace to is read from the shared
 * preferences.
 */
public class UploadingPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "UploadingPreferenceManager";
    private VehicleDataSink mUploader;

    public UploadingPreferenceManager(Context context) {
        super(context);
    }

    public void close() {
        super.close();
        stopUploading();
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.uploading_checkbox_key,
                R.string.uploading_path_key,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setUploadingStatus(getPreferences().getBoolean(getString(
                                R.string.uploading_checkbox_key), false));
            }
        };
    }

    private void setUploadingStatus(boolean enabled) {
        Log.i(TAG, "Setting uploading to " + enabled);
        if(enabled) {
            String path = getPreferenceString(R.string.uploading_path_key);
            if(!UploaderSink.validatePath(path)) {
                String error = "Target URL in preferences not valid " +
                        "-- not starting uploading a trace";
                Log.w(TAG, error);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = getPreferences().edit();
                editor.putBoolean(getString(R.string.uploading_checkbox_key),
                        false);
                editor.commit();
            } else {
                if(mUploader != null) {
                    stopUploading();
                }

                try {
                    mUploader = new UploaderSink(getContext(), path);
                } catch(DataSinkException e) {
                    Log.w(TAG, "Unable to add uploader sink", e);
                    return;
                }
                getVehicleManager().addSink(mUploader);
            }
        } else {
            stopUploading();
        }
    }

    private void stopUploading() {
        if(getVehicleManager() != null){
            getVehicleManager().removeSink(mUploader);
            mUploader = null;
        }
    }
}
