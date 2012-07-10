package com.openxc.enabler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.openxc.sinks.UploaderSink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import android.preference.Preference.OnPreferenceChangeListener;

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
        private BluetoothAdapter mBluetoothAdapter;
        private ListPreference mBluetoothDeviceListPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter == null) {
                String message = "This device most likely does not have " +
                        "a Bluetooth adapter";
                Log.w(TAG, message);
                return;
            }

            addPreferencesFromResource(R.xml.data_source_preferences);
            mBluetoothDeviceListPreference = (ListPreference)
                    findPreference(getString(R.string.bluetooth_mac_key));
            mBluetoothDeviceListPreference.setOnPreferenceChangeListener(
                    mBluetoothDeviceListener);

            fillDeviceList(mBluetoothDeviceListPreference);
            findPreference(getString(R.string.bluetooth_checkbox_key))
                .setOnPreferenceChangeListener(mBluetoothCheckboxListener);

            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(
                            getActivity());
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

        private void fillDeviceList(ListPreference preference) {
            Log.d(TAG, "Starting device discovery");
            Set<BluetoothDevice> pairedDevices =
                mBluetoothAdapter.getBondedDevices();
            ArrayList<String> entries = new ArrayList<String>();
            ArrayList<String> values = new ArrayList<String>();
            for(BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Found paired device: " + device);
                entries.add(device.getName() + " (" + device.getAddress() +
                        ")");
                values.add(device.getAddress());
            }
            CharSequence[] sample = {};
            preference.setEntries(entries.toArray(sample));
            preference.setEntryValues(values.toArray(sample));
        }
    }

    private class PreferenceListener implements
        SharedPreferences.OnSharedPreferenceChangeListener {
            public void onSharedPreferenceChanged(SharedPreferences preferences,
                    String key) {
                if(key.equals(getString(R.string.uploading_path_key))) {
                    String path = preferences.getString(key, null);
                    if(path != null && key.equals(getString(
                                    R.string.uploading_path_key))
                        && !UploaderSink.validatePath(path)) {
                        String error = "Invalid target URL \"" + path +
                            "\" -- must be an absolute URL " +
                            "with http:// prefix";
                        Toast.makeText(getApplicationContext(), error,
                                Toast.LENGTH_SHORT).show();
                        Log.w(TAG, error);
                    }
                }
            }
        };
}
