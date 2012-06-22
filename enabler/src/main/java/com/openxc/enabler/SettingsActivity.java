package com.openxc.enabler;

import java.net.URI;
import java.util.List;

import com.openxc.sinks.FileRecorderSink;
import com.openxc.sinks.UploaderSink;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
    private static String TAG = "SettingsActivity";

    private PreferenceListener mPreferenceListener;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPreferences.registerOnSharedPreferenceChangeListener(
                mPreferenceListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(
                mPreferenceListener);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public static class RecordingPreferences extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.recording_preferences);
        }
    }

    public static class DataSourcePreferences extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.data_source_preferences);
        }
    }

    private class PreferenceListener implements
        SharedPreferences.OnSharedPreferenceChangeListener {
            public void onSharedPreferenceChanged(SharedPreferences preferences,
                    String key) {
                if(key.equals(getString(R.string.uploading_path_key))
                        || key.equals(getString(R.string.recording_path_key))) {
                    String path = preferences.getString(key, null);
                    if(path != null) {
                        String error = null;
                        if(key.equals(getString(R.string.uploading_path_key))
                            && !UploaderSink.validatePath(path)) {
                            error = "Invalid target URL \"" + path +
                                "\" -- must be an absolute URL " +
                                "with http:// prefix";
                        } else if(!FileRecorderSink.validatePath(path)) {
                            error = "Invalid output directory \"" + path +
                                "\" choose directory such " +
                                "as /sdcard/openxc";
                        }

                        if(error != null) {
                            Toast.makeText(getApplicationContext(), error,
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG, error);
                        }
                    }
                }
            }
        };
}
