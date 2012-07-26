package com.openxc.enabler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.openxc.sinks.UploaderSink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.os.Build;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private BluetoothAdapter mBluetoothAdapter;
    private ListPreference mBluetoothDeviceListPreference;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceListener();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.recording_preferences);
            addPreferencesFromResource(R.xml.data_source_preferences);
            mBluetoothDeviceListPreference = (ListPreference)
                    findPreference(getString(R.string.bluetooth_mac_key));
            initialize(mBluetoothDeviceListPreference,
                    findPreference(getString(R.string.bluetooth_checkbox_key)));
        }
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
        }
    }

    public static class DataSourcePreferences extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.data_source_preferences);
            ((SettingsActivity)getActivity()).initialize(
                (ListPreference)
                findPreference(getString(R.string.bluetooth_mac_key)),
                findPreference(getString(R.string.bluetooth_checkbox_key)));
        }
    }

    protected void initialize(ListPreference listPreference,
            Preference checkboxPreference) {
        mBluetoothDeviceListPreference = listPreference;
        mBluetoothDeviceListPreference.setOnPreferenceChangeListener(
                mBluetoothDeviceListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            String message = "This device most likely does not have " +
                "a Bluetooth adapter -- skipping device search";
            Log.w(TAG, message);
        }

        fillDeviceList(mBluetoothDeviceListPreference);

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

    private void fillDeviceList(final ListPreference preference) {
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
                if(BluetoothDevice.ACTION_FOUND.equals(
                            intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE);
                    if(device.getBondState() !=
                            BluetoothDevice.BOND_BONDED) {
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
                        preference.setEntryValues(
                                values.toArray(prototype));
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
