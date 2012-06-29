package com.openxc.enabler;

import java.util.TimerTask;
import java.util.Timer;

import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;


import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import android.util.Log;

import android.widget.TextView;

import com.openxc.R;
import com.openxc.VehicleManager;

public class OpenXcEnablerActivity extends Activity {

    private static String TAG = "OpenXcEnablerActivity";

    private final Handler mHandler = new Handler();
    private TextView mVehicleManagerStatusView;;
    private TextView mMessageCountView;
    private TimerTask mUpdateMessageCountTask;
    private Timer mTimer;
    private VehicleManager mVehicleManager;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleManagerStatusView.setText("Running");
                }
            });

            mUpdateMessageCountTask = new MessageCountTask(mVehicleManager,
                    mHandler, mMessageCountView);
            mTimer = new Timer();
            mTimer.schedule(mUpdateMessageCountTask, 100, 1000);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleManagerStatusView.setText("Not running");
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG, "OpenXC Enabler created");

        startService(new Intent(this, VehicleManager.class));

        mVehicleManagerStatusView = (TextView) findViewById(
                R.id.vehicle_service_status);
        mMessageCountView = (TextView) findViewById(R.id.message_count);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OpenXC Enabler resumed");
        bindService(new Intent(this, VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);
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
        TurnSignalStatus command;
        switch (item.getItemId()) {
        case R.id.settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case R.id.left_turn:
            command = new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.LEFT);
            try {
                mVehicleManager.set(command);
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "Unable to send turn signal command", e);
            }
            return true;
        case R.id.right_turn:
            command = new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.RIGHT);
            try {
                mVehicleManager.set(command);
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "Unable to send turn signal command", e);
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
