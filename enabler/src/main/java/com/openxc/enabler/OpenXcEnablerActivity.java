package com.openxc.enabler;

import java.util.TimerTask;
import java.util.Timer;

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
    private TextView mRecordingStatusView;
    private TextView mMessageCountView;
    private TimerTask mUpdateMessageCountTask;
    private Timer mTimer;
    private VehicleService mVehicleService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleService");
            mVehicleService = ((VehicleService.VehicleServiceBinder)service
                    ).getService();

            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleServiceStatusView.setText("Running");
                }
            });

            mUpdateMessageCountTask = new MessageCountTask(mVehicleService,
                    mHandler, mMessageCountView);
            mTimer = new Timer();
            mTimer.schedule(mUpdateMessageCountTask, 100, 1000);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG, "OpenXC Enabler created");

        startService(new Intent(this, VehicleService.class));

        mVehicleServiceStatusView = (TextView) findViewById(
                R.id.vehicle_service_status);
        mRecordingStatusView = (TextView) findViewById(R.id.recording_status);
        mMessageCountView = (TextView) findViewById(R.id.message_count);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OpenXC Enabler resumed");
        bindService(new Intent(this, VehicleService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        updateRecordingStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mConnection != null) {
            unbindService(mConnection);
        }
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

    private void updateRecordingStatus() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final boolean recordingEnabled = preferences.getBoolean(
                getString(R.string.recording_checkbox_key), false);
        mHandler.post(new Runnable() {
            public void run() {
                if(recordingEnabled) {
                    mRecordingStatusView.setText("Enabled");
                } else {
                    mRecordingStatusView.setText("Disabled");
                }
            }
        });
    }
}
