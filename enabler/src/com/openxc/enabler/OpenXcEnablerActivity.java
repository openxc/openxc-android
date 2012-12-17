package com.openxc.enabler;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.BluetoothSourcePreferenceManager;
import com.openxc.enabler.preferences.FileRecordingPreferenceManager;
import com.openxc.enabler.preferences.GpsOverwritePreferenceManager;
import com.openxc.enabler.preferences.NativeGpsSourcePreferenceManager;
import com.openxc.enabler.preferences.UploadingPreferenceManager;
import com.openxc.enabler.preferences.VehiclePreferenceManager;

public class OpenXcEnablerActivity extends Activity {
    private static String TAG = "OpenXcEnablerActivity";

    private TextView mVehicleManagerStatusView;;
    private TextView mMessageCountView;
    private ListView mSourceListView;
    private ListView mSinkListView;
    private TimerTask mUpdateMessageCountTask;
    private TimerTask mUpdatePipelineStatusTask;
    private Timer mTimer;
    private VehicleManager mVehicleManager;
    private List<VehiclePreferenceManager> mPreferenceManagers =
            new ArrayList<VehiclePreferenceManager>();

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            new Thread(new Runnable() {
                public void run() {
                    mVehicleManager.waitUntilBound();
                    OpenXcEnablerActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            mVehicleManagerStatusView.setText("Running");
                        }
                    });
                }
            }).start();

            mPreferenceManagers.add(new BluetoothSourcePreferenceManager(
                        OpenXcEnablerActivity.this, mVehicleManager));
            mPreferenceManagers.add(new FileRecordingPreferenceManager(
                        OpenXcEnablerActivity.this, mVehicleManager));
            mPreferenceManagers.add(new GpsOverwritePreferenceManager(
                        OpenXcEnablerActivity.this, mVehicleManager));
            mPreferenceManagers.add(new NativeGpsSourcePreferenceManager(
                        OpenXcEnablerActivity.this, mVehicleManager));
            mPreferenceManagers.add(new UploadingPreferenceManager(
                        OpenXcEnablerActivity.this, mVehicleManager));

            mUpdateMessageCountTask = new MessageCountTask(mVehicleManager,
                    OpenXcEnablerActivity.this, mMessageCountView);
            mUpdatePipelineStatusTask = new PipelineStatusUpdateTask(
                    mVehicleManager, OpenXcEnablerActivity.this,
                    mSourceListView, mSinkListView);
            mTimer = new Timer();
            mTimer.schedule(mUpdateMessageCountTask, 100, 1000);
            mTimer.schedule(mUpdatePipelineStatusTask, 100, 1000);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
            OpenXcEnablerActivity.this.runOnUiThread(new Runnable() {
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
        mSourceListView = (ListView) findViewById(R.id.source_list);
        mSinkListView = (ListView) findViewById(R.id.sink_list);

        OpenXcEnablerActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mVehicleManagerStatusView.setText("Not running");
            }
        });

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
    public void onDestroy() {
        super.onDestroy();
        for(VehiclePreferenceManager manager : mPreferenceManagers) {
            manager.close();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
}
