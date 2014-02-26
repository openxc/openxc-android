package com.openxc.enabler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxc.sinks.UploaderSink;

/**
 * Initialize and display all preferences for the OpenXC Enabler application.
 *
 * In order to select a trace file to use as a data source, the device must have
 * a file manager application installed that responds to the GET_CONTENT intent,
 * e.g. OI File Manager.
 */
@TargetApi(12)
public class SettingsActivity extends PreferenceActivity {
    private static String TAG = "SettingsActivity";
    private final static String RECORDING_PREFERENCE =
            "com.openxc.enabler.preferences.RECORDING";
    private final static String DATA_SOURCE_PREFERENCE =
            "com.openxc.enabler.preferences.DATA_SOURCE";
    private final static String OUTPUT_PREFERENCE =
            "com.openxc.enabler.preferences.OUTPUT";
    private final static String ABOUT_PREFERENCE =
            "com.openxc.enabler.preferences.ABOUT";
    private final static int FILE_SELECTOR_RESULT = 100;

    private ListPreference mBluetoothDeviceListPreference;
    private CheckBoxPreference mBluetoothPollingPrefernce;
    private CheckBoxPreference mUploadingPreference;
    private Preference mTraceFilePreference;
    private CheckBoxPreference mTraceEnabledPreference;
    private CheckBoxPreference mNetworkSourcePreference;
    private EditTextPreference mNetworkHostPreference;
    private EditTextPreference mNetworkPortPreference;
    private Preference mAboutVersionPreference;
    private PreferenceManagerService mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLegacyLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPreferenceManager != null) {
            unbindService(mConnection);
            mPreferenceManager = null;
        }
    }

    protected boolean isValidFragment(String fragmentName){
        if(RecordingPreferences.class.getName().equals(fragmentName)){
            return true;
        } else if(OutputPreferences.class.getName().equals(fragmentName)){
            return true;
        } else if(DataSourcePreferences.class.getName().equals(fragmentName)){
            return true;
        } else if(AboutPreferences.class.getName().equals(fragmentName)){
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void initializeLegacyLayout() {
        String action = getIntent().getAction();
        if(action != null) {
            if(action.equals(RECORDING_PREFERENCE)) {
                addPreferencesFromResource(R.xml.recording_preferences);

                initializeUploadingPreferences(
                    findPreference(getString(R.string.uploading_checkbox_key)),
                    findPreference(getString(R.string.uploading_path_key)));

            } else if(action.equals(DATA_SOURCE_PREFERENCE)) {
                addPreferencesFromResource(R.xml.data_source_preferences);

                initializeBluetoothPreferences(
                    findPreference(getString(R.string.bluetooth_mac_key)),
                    findPreference(getString(R.string.bluetooth_checkbox_key)),
                    findPreference(getString(R.string.bluetooth_polling_key)));

                initializeNetwork(
                    findPreference(getString(R.string.network_host_key)),
                    findPreference(getString(R.string.network_port_key)),
                    findPreference(getString(R.string.network_checkbox_key)));

                initializeTracePreferences(
                    findPreference(getString(R.string.trace_source_checkbox_key)),
                    findPreference(getString(R.string.trace_source_file_key)));
            } else if(action.equals(OUTPUT_PREFERENCE)) {
                addPreferencesFromResource(R.xml.output_preferences);
            } else if(action.equals(ABOUT_PREFERENCE)) {
                addPreferencesFromResource(R.xml.about_preferences);

                initializeAboutPreferences(
                    findPreference(getString(R.string.application_version_key)));
            }
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FILE_SELECTOR_RESULT && resultCode == RESULT_OK) {
            String newValue = data.getData().getPath();
            SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(getString(R.string.trace_source_file_key), newValue);
            editor.commit();

            updateSummary(mTraceFilePreference, newValue);
        }
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
            ((SettingsActivity)getActivity()).initializeUploadingPreferences(
                findPreference(getString(R.string.uploading_checkbox_key)),
                findPreference(getString(R.string.uploading_path_key)));
        }
    }

    public static class OutputPreferences extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.output_preferences);
        }
    }

    public static class DataSourcePreferences extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.data_source_preferences);

            ((SettingsActivity)getActivity()).initializeBluetoothPreferences(
                findPreference(getString(R.string.bluetooth_mac_key)),
                findPreference(getString(R.string.bluetooth_checkbox_key)),
                findPreference(getString(R.string.bluetooth_polling_key)));
            ((SettingsActivity) getActivity()).initializeNetwork(
                    findPreference(getString(R.string.network_host_key)),
                    findPreference(getString(R.string.network_port_key)),
                    findPreference(getString(R.string.network_checkbox_key)));
            ((SettingsActivity)getActivity()).initializeTracePreferences(
                findPreference(getString(R.string.trace_source_checkbox_key)),
                findPreference(getString(R.string.trace_source_file_key)));
        }
    }

    public static class AboutPreferences extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.about_preferences);

             ((SettingsActivity)getActivity()).initializeAboutPreferences(
                     findPreference(getString(R.string.application_version_key)));
        }
    }

    protected void initializeTracePreferences(Preference traceEnabledPreference,
            Preference traceFilePreference) {
        mTraceEnabledPreference = (CheckBoxPreference) traceEnabledPreference;
        mTraceFilePreference = traceFilePreference;
        mTraceFilePreference.setOnPreferenceClickListener(
                mTraceFileClickListener);
        mTraceFilePreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);
        mTraceEnabledPreference.setOnPreferenceChangeListener(
                mTraceCheckboxListener);


        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        mTraceFilePreference.setEnabled(preferences.getBoolean(
                    getString(R.string.trace_source_checkbox_key), false));
        updateSummary(mTraceFilePreference,
                preferences.getString(
                    getString(R.string.trace_source_file_key), null));
    }

    protected void initializeUploadingPreferences(
            Preference uploadingPreference,
            Preference uploadingPathPreference) {
        mUploadingPreference = (CheckBoxPreference) uploadingPreference;
        uploadingPathPreference.setOnPreferenceChangeListener(
                mUploadingPathPreferenceListener);

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        updateSummary(uploadingPathPreference,
                preferences.getString(
                    getString(R.string.uploading_path_key), null));
    }

    protected void updateSummary(Preference preference, Object currentValue) {
        String summary = null;
        if(currentValue != null) {
            summary = currentValue.toString();
        } else {
            summary = "No value set";
        }
        preference.setSummary(summary);
    }

    protected void initializeBluetoothPreferences(Preference listPreference,
            Preference enabledPreference, Preference pollingPreference) {
        mBluetoothPollingPrefernce = (CheckBoxPreference) pollingPreference;
        mBluetoothDeviceListPreference = (ListPreference) listPreference;
        mBluetoothDeviceListPreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);

        bindService(new Intent(SettingsActivity.this,
                    PreferenceManagerService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        List<String> entries = new ArrayList<String>();
        entries.add(getString(R.string.bluetooth_mac_automatic_option));
        List<String> values = new ArrayList<String>();
        values.add(getString(R.string.bluetooth_mac_automatic_summary));

        CharSequence[] prototype = {};
        mBluetoothDeviceListPreference.setEntries(entries.toArray(prototype));
        mBluetoothDeviceListPreference.setEntryValues(values.toArray(prototype));

        enabledPreference.setOnPreferenceChangeListener(
                mBluetoothCheckboxListener);

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        mBluetoothDeviceListPreference.setEnabled(preferences.getBoolean(
                    getString(R.string.bluetooth_checkbox_key), false));
        mBluetoothPollingPrefernce.setEnabled(preferences.getBoolean(
                    getString(R.string.bluetooth_checkbox_key), false));

        updateSummary(mBluetoothDeviceListPreference,
                preferences.getString(getString(
                        R.string.bluetooth_mac_key), null));
    }

    protected void initializeNetwork(Preference hostPreference,
            Preference portPreference,
            Preference checkboxPreference) {
        mNetworkSourcePreference = (CheckBoxPreference) checkboxPreference;
        mNetworkSourcePreference.setOnPreferenceChangeListener(
                mNetworkCheckboxListener);

        mNetworkHostPreference = (EditTextPreference) hostPreference;
        mNetworkHostPreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);

        mNetworkPortPreference = (EditTextPreference) portPreference;
        mNetworkPortPreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        mNetworkHostPreference.setEnabled(preferences.getBoolean(
                    getString(R.string.network_checkbox_key), false));
        mNetworkPortPreference.setEnabled(preferences.getBoolean(
                    getString(R.string.network_checkbox_key), false));

        updateSummary(mNetworkHostPreference,
                preferences.getString(getString(
                        R.string.network_host_key), null));

        updateSummary(mNetworkPortPreference,
                preferences.getString(getString(
                        R.string.network_port_key), null));
    }

    protected void initializeAboutPreferences(
            Preference aboutVersionPreference) {
        try {
            mAboutVersionPreference = aboutVersionPreference;

            String versionNumber = getPackageManager().getPackageInfo(
                getPackageName(), 0).versionName;

            updateSummary(mAboutVersionPreference, versionNumber);

        } catch (NameNotFoundException e) {
            Log.e(TAG, "Could not get application version.", e);
        }
    }


    private OnPreferenceChangeListener mNetworkCheckboxListener =
            new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            mNetworkHostPreference.setEnabled((Boolean)newValue);
            mNetworkPortPreference.setEnabled((Boolean)newValue);
            return true;
        }
    };

    private OnPreferenceChangeListener mUpdateSummaryListener =
        new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                updateSummary(preference, newValue);
                return true;
            }
        };

    private OnPreferenceChangeListener mBluetoothCheckboxListener =
        new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                mBluetoothDeviceListPreference.setEnabled((Boolean)newValue);
                mBluetoothPollingPrefernce.setEnabled((Boolean)newValue);
                return true;
            }
        };

    private OnPreferenceChangeListener mUploadingPathPreferenceListener =
        new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                String path = (String) newValue;
                if(!UploaderSink.validatePath(path)) {
                    String error = "Invalid target URL \"" + path +
                        "\" -- must be an absolute URL " +
                        "with http:// prefix";
                    Toast.makeText(getApplicationContext(), error,
                            Toast.LENGTH_SHORT).show();
                    Log.w(TAG, error);
                    mUploadingPreference.setChecked(false);
                }
                updateSummary(preference, newValue);
                return true;
            }
        };

    private OnPreferenceChangeListener mTraceCheckboxListener =
            new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            mTraceFilePreference.setEnabled((Boolean)newValue);
            return true;
        }
    };

    private Preference.OnPreferenceClickListener mTraceFileClickListener =
            new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_SELECTOR_RESULT);
            return true;
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to PreferenceManagerService");
            mPreferenceManager = ((PreferenceManagerService.PreferenceBinder)service
                    ).getService();

            List<String> entries = new ArrayList<String>();
            entries.add(getString(R.string.bluetooth_mac_automatic_summary));
            List<String> values = new ArrayList<String>();
            values.add(getString(R.string.bluetooth_mac_automatic_option));

            Map<String, String> discoveredDevices =
                    mPreferenceManager.getBluetoothDevices();
            values.addAll(discoveredDevices.keySet());
            entries.addAll(discoveredDevices.values());

            CharSequence[] prototype = {};
            mBluetoothDeviceListPreference.setEntries(
                    entries.toArray(prototype));
            mBluetoothDeviceListPreference.setEntryValues(
                    values.toArray(prototype));
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "PreferenceMangerService disconnected unexpectedly");
            mPreferenceManager = null;
        }
    };
}
