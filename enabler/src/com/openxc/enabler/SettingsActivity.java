package com.openxc.enabler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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
    private final static int FILE_SELECTOR_RESULT = 100;

    private BluetoothAdapter mBluetoothAdapter;
    private ListPreference mBluetoothDeviceListPreference;
    private CheckBoxPreference mUploadingPreference;
    private Preference mTraceFilePreference;
    private CheckBoxPreference mTraceEnabledPreference;
    private CheckBoxPreference mNetworkSourcePreference;
    private EditTextPreference mNetworkHostPreference;
    private EditTextPreference mNetworkPortPreference;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLegacyLayout();
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
                    findPreference(getString(R.string.bluetooth_checkbox_key)));

                initializeNetwork(
                    findPreference(getString(R.string.network_host_key)),
                    findPreference(getString(R.string.network_port_key)),
                    findPreference(getString(R.string.network_checkbox_key)));
            } else if(action.equals(OUTPUT_PREFERENCE)) {
                addPreferencesFromResource(R.xml.output_preferences);
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
    public void onDestroy() {
        super.onDestroy();
        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
            if(mBluetoothAdapter != null) {
                mBluetoothAdapter.cancelDiscovery();
            }
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
                findPreference(getString(R.string.bluetooth_checkbox_key)));
            ((SettingsActivity) getActivity()).initializeNetwork(
                    findPreference(getString(R.string.network_host_key)),
                    findPreference(getString(R.string.network_port_key)),
                    findPreference(getString(R.string.network_checkbox_key)));
            ((SettingsActivity)getActivity()).initializeTracePreferences(
                findPreference(getString(R.string.trace_source_checkbox_key)),
                findPreference(getString(R.string.trace_source_file_key)));
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
            Preference checkboxPreference) {
        mBluetoothDeviceListPreference = (ListPreference) listPreference;
        mBluetoothDeviceListPreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            String message = "This device most likely does not have " +
                "a Bluetooth adapter -- skipping device search";
            Log.w(TAG, message);
        }

        fillBluetoothDeviceList(mBluetoothDeviceListPreference);

        checkboxPreference.setOnPreferenceChangeListener(
                mBluetoothCheckboxListener);

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        mBluetoothDeviceListPreference.setEnabled(preferences.getBoolean(
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

    private void fillBluetoothDeviceList(final ListPreference preference) {
        ArrayList<String> entries = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        if(mBluetoothAdapter != null) {
            Log.d(TAG, "Starting paired device search");
            Set<BluetoothDevice> pairedDevices =
                mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Found paired device: " + device);
                entries.add(device.getName() + " (" + device.getAddress() +
                        ")");
                values.add(device.getAddress());
            }
        }

        CharSequence[] prototype = {};
        preference.setEntries(entries.toArray(prototype));
        preference.setEntryValues(values.toArray(prototype));

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE);
                    if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        List<CharSequence> entries =
                            new ArrayList<CharSequence>(
                                    Arrays.asList(preference.getEntries()));
                        List<CharSequence> values =
                            new ArrayList<CharSequence>(
                                    Arrays.asList(preference.getEntryValues()));
                        entries.add(device.getName() + " (" +
                                device.getAddress() + ")");
                        values.add(device.getAddress());
                        CharSequence[] prototype = {};
                        preference.setEntries(entries.toArray(prototype));
                        preference.setEntryValues(values.toArray(prototype));
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(
                BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        if(mBluetoothAdapter != null) {
            if(mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
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
}
