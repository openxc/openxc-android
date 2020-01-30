package com.openxc.enabler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxc.sinks.UploaderSink;
import com.openxcplatform.enabler.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.telephony.TelephonyManager;

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

    private static final int APP_PERMISSION_REQUEST_WRITE_STORAGE = 200;
    private static final int APP_PERMISSION_ACCESS_FINE_LOCATION = 300;

    private ListPreference mVehicleInterfaceListPreference;
    private ListPreference mBluetoothDeviceListPreference;
    private CheckBoxPreference mUploadingPreference;
    private Preference mSourceNamePreference;
    private CheckBoxPreference mTraceRecordingPreference;
    private CheckBoxPreference mDisableTracePlayingLoop;
    private CheckBoxPreference mDweetingPreference;
    private Preference mTraceFilePreference;
    private EditTextPreference mNetworkHostPreference;
    private EditTextPreference mNetworkPortPreference;
    private Preference mAboutVersionPreference;
    private PreferenceManagerService mPreferenceManager;
    private ListPreference mDataFormatListPreference;
    private CheckBoxPreference mPhoneSensorPreference;

    private PreferenceCategory mBluetoothPreferences;
    private PreferenceCategory mNetworkPreferences;
    private PreferenceCategory mTracePreferences;
    private boolean isTraceRecording;

    private TelephonyManager mTelephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLegacyLayout();
    }

    @Override
    public void onPause() {
        super.onPause();

        updateTargetURL();
        displaySourceName();

        if(mPreferenceManager != null) {
            unbindService(mConnection);
            mPreferenceManager = null;
        }
    }

    private void updateTargetURL() {
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String path = (settings.getString("uploading_target", ""));
            String baseEndpoint = path.substring(0, path.indexOf(".com")+4);
            String data = android.util.Base64.encodeToString(getDeviceID().getBytes(), android.util.Base64.NO_WRAP);
            path = baseEndpoint + "/api/v1/message/" + data + "/save";
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("uploading_target", path);
            editor.commit();
        } catch (Exception e) {
            Log.i(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    private void displaySourceName() {
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String path = getDeviceID();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("uploading_source_name", path);
            editor.commit();
        } catch (Exception e) {
            Log.i(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    protected boolean isValidFragment(String fragmentName){
        return RecordingPreferences.class.getName().equals(fragmentName) ||
                OutputPreferences.class.getName().equals(fragmentName) ||
                DataSourcePreferences.class.getName().equals(fragmentName) ||
                AboutPreferences.class.getName().equals(fragmentName);
    }

    @SuppressWarnings("deprecation")
    private void initializeLegacyLayout() {
        String action = getIntent().getAction();
        if(action != null) {
            if(action.equals(RECORDING_PREFERENCE)) {
                addPreferencesFromResource(R.xml.recording_preferences);
                initializeUploadingPreferences(getPreferenceManager());
                initializeDweetingPreferences(getPreferenceManager());
                initializeTraceRecordingPreferences(getPreferenceManager());
            } else if(action.equals(DATA_SOURCE_PREFERENCE)) {
                addPreferencesFromResource(R.xml.data_source_preferences);
                initializeDataSourcePreferences(getPreferenceManager());
            } else if(action.equals(OUTPUT_PREFERENCE)) {
                addPreferencesFromResource(R.xml.output_preferences);
            } else if(action.equals(ABOUT_PREFERENCE)) {
                addPreferencesFromResource(R.xml.about_preferences);
                initializeAboutPreferences(getPreferenceManager());
            }
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }

    // Thanks to Paul Burke on Stack Overflow
    @TargetApi(Build.VERSION_CODES.KITKAT)
	// (http://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content)
    @SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if(isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if(isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if(isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    return id.replace("raw:", "");
                }
                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    Log.i(TAG,e.getMessage());
                    return null;
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme()) ||
                "content".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }




    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
            String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
            column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FILE_SELECTOR_RESULT && resultCode == RESULT_OK) {
            String newValue = getPath(this, data.getData());
            SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putString(getString(R.string.trace_source_file_key), newValue);
            editor.commit();
            Log.d(TAG, "initializtraceFilePreference: "+ newValue);
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
                getPreferenceManager());
            ((SettingsActivity)getActivity()).initializeDweetingPreferences(
                    getPreferenceManager());
            ((SettingsActivity)getActivity()).initializeTraceRecordingPreferences(
                    getPreferenceManager());
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

            ((SettingsActivity)getActivity()).initializeDataSourcePreferences(
                getPreferenceManager());
        }
    }

    public static class AboutPreferences extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.about_preferences);

             ((SettingsActivity)getActivity()).initializeAboutPreferences(
                     getPreferenceManager());
        }
    }

    protected void initializeTracePreferences(PreferenceManager manager) {
        mTraceFilePreference = manager.findPreference(
                getString(R.string.trace_source_file_key));
        mTraceFilePreference.setOnPreferenceClickListener(
                mTraceFileClickListener);
        mTraceFilePreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);

//        SharedPreferences preferences =
//            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        updateSummary(mTraceFilePreference,
//                preferences.getString(
//                    getString(R.string.trace_source_file_key), null));
    }

    protected void initializePhoneSensorPreferences(PreferenceManager manager) {
        mPhoneSensorPreference = (CheckBoxPreference) manager.findPreference(
                getString(R.string.phone_source_polling_checkbox_key));
        mPhoneSensorPreference.setOnPreferenceClickListener(
                mPhoneSensorClickListener);

    }

    protected void initializeUploadingPreferences(PreferenceManager manager) {
        mUploadingPreference = (CheckBoxPreference) manager.findPreference(getString(R.string.uploading_checkbox_key));
        mSourceNamePreference = manager.findPreference(getString(R.string.uploading_source_name_key));
        Preference uploadingPathPreference = manager.findPreference(getString(R.string.uploading_path_key));
        uploadingPathPreference.setOnPreferenceChangeListener(mUploadingPathPreferenceListener);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateSummary(uploadingPathPreference, preferences.getString(
                    getString(R.string.uploading_path_key), null));
        updateSummary(mSourceNamePreference, preferences.getString(
                getString(R.string.uploading_source_name_key), null));
    }

    protected void initializeTraceRecordingPreferences(PreferenceManager manager) {
        mTraceRecordingPreference = (CheckBoxPreference) manager.findPreference(
                getString(R.string.recording_checkbox_key));
        mTraceRecordingPreference.setOnPreferenceClickListener(mTraceFileRecordingClickListener);
    }
    protected void initializeDisableTracePlayingLoopPreferences(PreferenceManager manager) {
        mDisableTracePlayingLoop = (CheckBoxPreference) manager.findPreference(
                getString(R.string.trace_source_playing_checkbox_key));
        mDisableTracePlayingLoop.setOnPreferenceClickListener(mDisableTracePlayingLoopClickListener);
    }
    protected void initializeDweetingPreferences(PreferenceManager manager) {
        mDweetingPreference = (CheckBoxPreference) manager.findPreference(
                getString(R.string.dweeting_checkbox_key));
        Preference dweetingPathPreference = manager.findPreference(
                getString(R.string.dweeting_thingname_key));
        dweetingPathPreference.setOnPreferenceChangeListener(
                mDweetingPathPreferenceListener);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateSummary(dweetingPathPreference,
                preferences.getString(
                        getString(R.string.dweeting_thingname_key), null));
    }

    protected void initializeVehicleInterfacePreference(PreferenceManager manager) {
        mVehicleInterfaceListPreference = (ListPreference)
                        manager.findPreference(getString(
                                R.string.vehicle_interface_key));
        PreferenceManager.setDefaultValues(this, R.xml.data_source_preferences, false);
        mVehicleInterfaceListPreference.setOnPreferenceChangeListener(
                mVehicleInterfaceUpdatedListener);

        PreferenceScreen screen = (PreferenceScreen)
                manager.findPreference("preference_screen");
        mBluetoothPreferences = (PreferenceCategory) screen.findPreference(
                getString(R.string.bluetooth_settings));
        mNetworkPreferences = (PreferenceCategory) screen.findPreference(
                getString(R.string.network_settings));
        mTracePreferences = (PreferenceCategory) screen.findPreference(
                getString(R.string.trace_source_settings));

        List<String> entries = new ArrayList<>(Arrays.asList(getResources().
                    getStringArray(R.array.vehicle_interface_types)));
        List<String> values = new ArrayList<>(Arrays.asList(getResources().
                    getStringArray(R.array.vehicle_interface_type_aliases)));
        if(android.os.Build.VERSION.SDK_INT <
                android.os.Build.VERSION_CODES.HONEYCOMB) {
            // USB not supported, so re-load entries without that option
            entries.remove(getString(R.string.usb_interface_option));
            values.remove(getString(R.string.usb_interface_option_value));
        }

        if(BluetoothAdapter.getDefaultAdapter() == null) {
            // No Bluetooth adapter, so remove those entries too
            entries.remove(getString(R.string.bluetooth_interface_option));
            values.remove(getString(R.string.bluetooth_interface_option_value));
            screen.removePreference(mBluetoothPreferences);

            // Bluetooth is the default, so we need to force it to None if the
            // device has no adapter
            if(mVehicleInterfaceListPreference.getValue().equals(
                        getString(R.string.bluetooth_interface_option_value))) {
                mVehicleInterfaceListPreference.setValueIndex(
                        mVehicleInterfaceListPreference.findIndexOfValue(
                            getString(
                                R.string.disabled_interface_option_value)));
            }
        }

        CharSequence[] prototype = {};
        mVehicleInterfaceListPreference.setEntries(entries.toArray(prototype));
        mVehicleInterfaceListPreference.setEntryValues(values.toArray(prototype));
        mVehicleInterfaceListPreference.setSummary(
                mVehicleInterfaceListPreference.getEntry());

        mBluetoothPreferences.setEnabled(mVehicleInterfaceListPreference.
                getValue().equals(
                    getString(R.string.bluetooth_interface_option_value)));
        mNetworkPreferences.setEnabled(mVehicleInterfaceListPreference.
                getValue().equals(
                    getString(R.string.network_interface_option_value)));
        mTracePreferences.setEnabled(mVehicleInterfaceListPreference.
                getValue().equals(
                    getString(R.string.trace_interface_option_value)));
    }
    //Ranjan Added code for Data format
    protected void initializDataformatPreference(PreferenceManager manager) {
        mDataFormatListPreference = (ListPreference)
                manager.findPreference(getString(
                        R.string.data_format_key));
        PreferenceManager.setDefaultValues(this, R.xml.data_source_preferences, false);
        mDataFormatListPreference.setOnPreferenceChangeListener(
                mDataFormatUpdatedListener);

        PreferenceScreen screen = (PreferenceScreen)
                manager.findPreference("preference_screen");

        List<String> entries = new ArrayList<>(Arrays.asList(getResources().
                getStringArray(R.array.data_format_types)));
        List<String> values = new ArrayList<>(Arrays.asList(getResources().
                getStringArray(R.array.data_format_type_aliases)));
        if(android.os.Build.VERSION.SDK_INT <
                android.os.Build.VERSION_CODES.HONEYCOMB) {
            // USB not supported, so re-load entries without that option
            entries.remove(getString(R.string.usb_interface_option));
            values.remove(getString(R.string.usb_interface_option_value));
        }

        CharSequence[] prototype = {};
        mDataFormatListPreference.setEntries(entries.toArray(prototype));
        mDataFormatListPreference.setEntryValues(values.toArray(prototype));
        mDataFormatListPreference.setSummary(mDataFormatListPreference.getEntry());
        Log.d(TAG, "initializDataformatPreference: "+ mDataFormatListPreference.getEntry());

    }

    protected void initializeDataSourcePreferences(PreferenceManager manager) {
        initializeVehicleInterfacePreference(manager);
        initializeBluetoothPreferences(manager);
        initializeNetwork(manager);
        initializeTracePreferences(manager);
        initializePhoneSensorPreferences(manager);
        initializDataformatPreference(manager);
        initializeDisableTracePlayingLoopPreferences(manager);
    }

    protected void initializeBluetoothPreferences(PreferenceManager manager) {
        mBluetoothDeviceListPreference = (ListPreference)
                    manager.findPreference(getString(R.string.bluetooth_mac_key));
        // If the device doesn't have BT, we removed these preferences earlier
        // in the initialization
        if(mBluetoothDeviceListPreference != null) {
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

            SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            updateSummary(mBluetoothDeviceListPreference,
                    preferences.getString(getString(
                            R.string.bluetooth_mac_key), null));
        }
    }

    protected void initializeNetwork(PreferenceManager manager) {
        mNetworkHostPreference = (EditTextPreference)
                    manager.findPreference(getString(R.string.network_host_key));
        mNetworkHostPreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);

        mNetworkPortPreference = (EditTextPreference)
                    manager.findPreference(getString(R.string.network_port_key));
        mNetworkPortPreference.setOnPreferenceChangeListener(
                mUpdateSummaryListener);

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        updateSummary(mNetworkHostPreference,
                preferences.getString(getString(
                        R.string.network_host_key), null));

        updateSummary(mNetworkPortPreference,
                preferences.getString(getString(
                        R.string.network_port_key), null));
    }

    protected void initializeAboutPreferences(PreferenceManager manager) {
        try {
            mAboutVersionPreference = manager.findPreference(
                    getString(R.string.application_version_key));

            String versionNumber = getPackageManager().getPackageInfo(
                getPackageName(), 0).versionName;

            updateSummary(mAboutVersionPreference, versionNumber);

        } catch (NameNotFoundException e) {
            Log.e(TAG, "Could not get application version.", e);
        }
    }
    //Ranjan Added code for Data format
    private OnPreferenceChangeListener mDataFormatUpdatedListener =
            new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    // Can't just call preference.getSummary() because this callback
                    // happens before newValue is actually set.
                    ListPreference listPreference = (ListPreference) preference;
                    String newSummary = listPreference.getEntries()[
                            listPreference.findIndexOfValue(
                                    newValue.toString())].toString();
                    preference.setSummary(newSummary);
                    // mDataFormatListPreference.setEnabled(newValue.equals(getString(R.string.)));
                    Log.d(TAG, "initializDataformatPreference: "+ preference.getSummary());

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("dataFormat", newSummary); // Storing string
                    editor.commit();

                    return true;
                }
            };

    protected void updateSummary(Preference preference, Object currentValue) {
        String summary = null;
        if(currentValue != null) {
            summary = currentValue.toString();
        } else {
            summary = "No value set";
        }
        preference.setSummary(summary);
    }

    private OnPreferenceChangeListener mVehicleInterfaceUpdatedListener =
            new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            // Can't just call preference.getSummary() because this callback
            // happens bofore newValue is actually set.
            ListPreference listPreference = (ListPreference) preference;
            String newSummary = listPreference.getEntries()[
                    listPreference.findIndexOfValue(
                            newValue.toString())].toString();
            preference.setSummary(newSummary);

            mNetworkPreferences.setEnabled(newValue.equals(
                    getString(R.string.network_interface_option_value)));
            mBluetoothPreferences.setEnabled(newValue.equals(
                    getString(R.string.bluetooth_interface_option_value)));
            mTracePreferences.setEnabled(newValue.equals(
                    getString(R.string.trace_interface_option_value)));

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = pref.edit();
            if(newSummary.equals("Pre-recorded Trace")) {
                editor.putBoolean("isTracePlayingEnabled", true);
            }else{
                editor.putBoolean("isTracePlayingEnabled", false);// Storing boolean
            }
            editor.commit();
           // Log.d(TAG, "initializDataformatPreference: "+ getString(R.string.trace_interface_option_value));
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

    private OnPreferenceChangeListener mUploadingPathPreferenceListener =
            new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
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
                String baseEndpoint = path.substring(0, path.indexOf(".com")+4);
                String data = android.util.Base64.encodeToString(getDeviceID().getBytes(), android.util.Base64.NO_WRAP);
                path = baseEndpoint + "/api/v1/message/" + data + "/save";
                newValue = path;
                mSourceNamePreference.setSummary(getDeviceID());
            }

            updateSummary(preference, newValue);
            return true;
        }
    };


    private String getDeviceID() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "device_id_not_available";
        } else {
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = mTelephonyManager.getDeviceId();
            return deviceId;
        }
    }

    private OnPreferenceChangeListener mDweetingPathPreferenceListener =
            new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    String path = (String) newValue;
                    updateSummary(preference, newValue);
                    return true;
                }
    };


    private Preference.OnPreferenceClickListener mTraceFileClickListener =
            new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (sharedpreferences != null) {
              isTraceRecording = sharedpreferences.getBoolean("IsTraceRecording", false);
                Log.d("BytestreamDataSource", "TraceRecording: " + isTraceRecording);
            }
            if(!isTraceRecording){
                Log.d(TAG, "Tracefile checklist:");
                checkExternalStoragePermission();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, FILE_SELECTOR_RESULT);

            }else{
                Toast.makeText(getApplicationContext(),"Please stop Tracefile Recording",Toast.LENGTH_SHORT).show();
               // Log.d(TAG, "Tracefile checklist else:" );
            }
            return true;
        }
    };

    private Preference.OnPreferenceClickListener mTraceFileRecordingClickListener =
            new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean isTracePlaying = sharedpreferences.getBoolean("isTracePlayingEnabled", false);
                    Log.d(TAG, "Tracefile checklist recordvalue:" + isTracePlaying);
                    if (sharedpreferences != null && !isTracePlaying) {
                        //mTraceRecordingPreference.setChecked(true);

                    }else{
                        Toast.makeText(getApplicationContext(),"Please stop Tracefile Playing",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Tracefile checklist record:");
                        mTraceRecordingPreference.setChecked(false);
                    }

                    return false;
                }
            };

    private Preference.OnPreferenceClickListener mDisableTracePlayingLoopClickListener =
            new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("isDisabledTracePlayingLoop", mDisableTracePlayingLoop.isChecked());
                    editor.commit();
                    return false;
                }
            };

    private Preference.OnPreferenceClickListener mPhoneSensorClickListener =
            new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    checkPhoneSensorPermission();
                    return false;
                }
            };

    private void checkPhoneSensorPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    APP_PERMISSION_ACCESS_FINE_LOCATION);

        }
    }

    private void checkExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    APP_PERMISSION_REQUEST_WRITE_STORAGE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case APP_PERMISSION_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do nothing
                } else {
                    Toast.makeText(this, "External write permission missing", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case APP_PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do nothing
                    mPhoneSensorPreference.setChecked(true);
                } else {
                    Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
                    mPhoneSensorPreference.setChecked(false);
                }
                return;
            }
        }
    }
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
