package com.openxc.enabler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.preference.Preference;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import android.util.Log;

import android.widget.TextView;

import com.openxc.VehicleService;
import com.openxc.remote.RemoteVehicleServiceException;

public class OpenXcEnablerActivity extends Activity {

    private static String TAG = "OpenXcEnablerActivity";

    private final Handler mHandler = new Handler();
    private TextView mVehicleServiceStatusView;;
    private VehicleService mVehicleService;
    private RecordingEnabledPreferenceListener mRecordingPreferenceListener;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleService");
            mVehicleService = ((VehicleService.VehicleServiceBinder)service
                    ).getService();
            setRecordingStatus();

            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleServiceStatusView.setText("Running");
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "RemoteVehicleService disconnected unexpectedly");
            mVehicleService = null;
            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleServiceStatusView.setText("Not running");
                }
            });
        }
    };

    private void setRecordingStatus() {
        if(mVehicleService == null) {
            return;
        }
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(
                        OpenXcEnablerActivity.this);
        try {
            mVehicleService.enableRecording(preferences.getBoolean(
                        getString(R.string.recording_checkbox_key), false));
        } catch(RemoteVehicleServiceException e) {
            Log.w(TAG, "Unable to set recording status", e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG, "OpenXC Enabler created");

        mVehicleServiceStatusView = (TextView) findViewById(
                R.id.vehicle_service_status);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(
                        OpenXcEnablerActivity.this);
        mRecordingPreferenceListener =
                new RecordingEnabledPreferenceListener();
        preferences.registerOnSharedPreferenceChangeListener(
                mRecordingPreferenceListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(
                        OpenXcEnablerActivity.this);
        preferences.unregisterOnSharedPreferenceChangeListener(
                mRecordingPreferenceListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OpenXC Enabler resumed");
        bindService(new Intent(this, VehicleService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private class RecordingEnabledPreferenceListener
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            if(key.equals(getString(R.string.recording_checkbox_key))) {
                setRecordingStatus();
            }
        }
    }
}
