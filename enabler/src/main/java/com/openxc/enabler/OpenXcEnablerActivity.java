package com.openxc.enabler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.TimerTask;
import java.util.Timer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import android.util.Log;

import android.widget.TextView;
import android.widget.Toast;

import com.openxc.R;
import com.openxc.VehicleManager;

public class OpenXcEnablerActivity extends Activity
    //implements OnSharedPreferenceChangeListener
    {

    private static String TAG = "OpenXcEnablerActivity";

    private final Handler mHandler = new Handler();
    private TextView mVehicleManagerStatusView;;
    private TextView mMessageCountView;
    private TimerTask mUpdateMessageCountTask;
    private Timer mTimer;
    private VehicleManager mVehicleManager;
    private PreferenceListener listener;
    private SharedPreferences preferences;

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


        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new PreferenceListener();
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OpenXC Enabler resumed");
        bindService(new Intent(this, VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mConnection != null) {
            unbindService(mConnection);
        }
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
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
    private class PreferenceListener
    implements SharedPreferences.OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {

            if(key.equals(getString(R.string.uploading_path_key))) {
                try {
                    URI uri = new URI(getString(R.string.uploading_path_key));
                    if(!uri.isAbsolute()) {
                        Toast.makeText(getApplicationContext(), "Invalid URL",
                                Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Invalid target URL set");
                    }
                } catch(java.net.URISyntaxException e) {
                    Log.w(TAG, "Target URL in preferences not valid ", e);
                }
            }
            else return;
        }
    }

}
