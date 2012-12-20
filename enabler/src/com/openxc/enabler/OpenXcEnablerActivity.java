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
import com.openxc.enabler.preferences.BluetoothPreferenceManager;
import com.openxc.enabler.preferences.FileRecordingPreferenceManager;
import com.openxc.enabler.preferences.GpsOverwritePreferenceManager;
import com.openxc.enabler.preferences.NativeGpsPreferenceManager;
import com.openxc.enabler.preferences.NetworkPreferenceManager;
import com.openxc.enabler.preferences.TraceSourcePreferenceManager;
import com.openxc.enabler.preferences.UploadingPreferenceManager;
import com.openxc.enabler.preferences.VehiclePreferenceManager;

/** The OpenXC Enabler app is primarily for convenience, but it also increases
 * the reliability of OpenXC by handling background tasks on behalf of client
 * applications.
 *
 * The Enabler provides a common location to control which data sources and
 * sinks are active, e.g. if the a trace file should be played back or recorded.
 * It's preferable to be able to change the data source on the fly, and not have
 * to programmatically load a trace file in any application under test.
 *
 * With the Enabler installed, the {@link com.openxc.remote.VehicleService} is
 * also started automatically when the Android device boots up. A simple data
 * sink like a trace file uploader can start immediately without any user
 * interaction.
 *
 * As a developer, you can also appreciate that because the Enabler takes care
 * of starting the {@link com.openxc.remote.VehicleService}, you don't need to
 * add much to your application's AndroidManifest.xml - just the
 * {@link com.openxc.VehicleManager} service.
*/
public class OpenXcEnablerActivity extends Activity {
    private static String TAG = "OpenXcEnablerActivity";

    private TextView mVehicleManagerStatusView;
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

            for(VehiclePreferenceManager manager : mPreferenceManagers) {
                manager.setVehicleManager(mVehicleManager);
            }

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

        mPreferenceManagers = new ArrayList<VehiclePreferenceManager>();
        mPreferenceManagers.add(new BluetoothPreferenceManager(
                    OpenXcEnablerActivity.this));
        mPreferenceManagers.add(new FileRecordingPreferenceManager(
                    OpenXcEnablerActivity.this));
        mPreferenceManagers.add(new GpsOverwritePreferenceManager(
                    OpenXcEnablerActivity.this));
        mPreferenceManagers.add(new NativeGpsPreferenceManager(
                    OpenXcEnablerActivity.this));
        mPreferenceManagers.add(new UploadingPreferenceManager(
                    OpenXcEnablerActivity.this));
        mPreferenceManagers.add(new NetworkPreferenceManager(
                    OpenXcEnablerActivity.this));
        mPreferenceManagers.add(new TraceSourcePreferenceManager(
                    OpenXcEnablerActivity.this));

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
