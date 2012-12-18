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

@TargetApi(12)
public class SettingsActivity extends PreferenceActivity {
    private static String TAG = "SettingsActivity";
    private final static String RECORDING_PREFERENCE =
            "com.openxc.enabler.preferences.RECORDING";
    private final static String DATA_SOURCE_PREFERENCE =
            "com.openxc.enabler.preferences.DATA_SOURCE";
    private final static String OUTPUT_PREFERENCE =
            "com.openxc.enabler.preferences.OUTPUT";

    private BluetoothAdapter mBluetoothAdapter;
    private ListPreference mBluetoothDeviceListPreference;
    private CheckBoxPreference mUploadingPreference;
    private EditTextPreference mEthernetConnectionPreference;
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

                initializeEthernet(
                    findPreference(getString(R.string.ethernet_connection_key)),
                    findPreference(getString(R.string.ethernet_checkbox_key)));
            } else if(action.equals(OUTPUT_PREFERENCE)) {
                addPreferencesFromResource(R.xml.output_preferences);
            }
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
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
            ((SettingsActivity) getActivity()).initializeEthernet(
                    findPreference(getString(R.string.ethernet_connection_key)),
                    findPreference(getString(R.string.ethernet_checkbox_key)));
        }
    }

    protected void initializeUploadingPreferences(
            Preference uploadingPreference,
            Preference uploadingPathPreference) {
        mUploadingPreference = (CheckBoxPreference) uploadingPreference;
        uploadingPathPreference.setOnPreferenceChangeListener(
                mUploadingPathPreferenceListener);
    }

    protected void initializeBluetoothPreferences(Preference listPreference,
            Preference checkboxPreference) {
        mBluetoothDeviceListPreference = (ListPreference) listPreference;
        mBluetoothDeviceListPreference.setOnPreferenceChangeListener(
                mBluetoothDeviceListener);

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

        String currentDevice = preferences.getString(
                getString(R.string.bluetooth_mac_key), null);
        String summary = null;
        if(currentDevice != null) {
            summary = "Currently using " + currentDevice;
        } else {
            summary = "No device selected";
        }
        mBluetoothDeviceListPreference.setSummary(summary);
    }

    protected void initializeEthernet(Preference editPreference,
            Preference checkboxPreference) {
        mEthernetConnectionPreference = (EditTextPreference) editPreference;
        mEthernetConnectionPreference.setOnPreferenceChangeListener(
                mEthernetConnectionListener);

        checkboxPreference.setOnPreferenceChangeListener(
                mEthernetCheckboxListener);

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        mEthernetConnectionPreference.setEnabled(preferences.getBoolean(
                    getString(R.string.ethernet_checkbox_key), false));

        String currentConnection = preferences.getString(getString(
                    R.string.ethernet_connection_key), null);
        String summary = null;
        if(currentConnection != null) {
            summary = "Currently using host " + currentConnection;
        } else {
            summary = "No server specified";
        }
        mEthernetConnectionPreference.setSummary(summary);
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

    private OnPreferenceChangeListener mEthernetConnectionListener =
            new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            preference.setSummary("Currently using " + newValue);
            return true;
        }
    };

    private OnPreferenceChangeListener mEthernetCheckboxListener =
            new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            mEthernetConnectionPreference.setEnabled((Boolean)newValue);
            return true;
        }
    };

    private OnPreferenceChangeListener mBluetoothDeviceListener =
        new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                preference.setSummary("Currently using " + newValue);
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
                } else {
                    preference.setSummary("Currently using " + newValue);
                }
                return true;
            }
        };
}
