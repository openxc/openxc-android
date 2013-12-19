package com.openxc.enabler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.crittercism.app.Crittercism;
import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxc.enabler.utils.AppConst;
import com.openxc.enabler.utils.AppUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

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

    private View mServiceNotRunningWarningView;
    private TextView mMessageCountView;
    private View mUnknownConnIV;
    private View mBluetoothConnIV;
    private View mUsbConnIV;
    private View mNetworkConnIV;
    private View mFileConnIV;
    private View mNoneConnView;
    private TimerTask mUpdateMessageCountTask;
    private TimerTask mUpdatePipelineStatusTask;
    private Timer mTimer;
    private VehicleManager mVehicleManager;

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
                            mServiceNotRunningWarningView.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();

            mUpdateMessageCountTask = new MessageCountTask(mVehicleManager,
                    OpenXcEnablerActivity.this, mMessageCountView);
            mUpdatePipelineStatusTask = new PipelineStatusUpdateTask(
                    mVehicleManager, OpenXcEnablerActivity.this,
                    mUnknownConnIV, mFileConnIV, mNetworkConnIV,
                    mBluetoothConnIV, mUsbConnIV, mNoneConnView);
            mTimer = new Timer();
            mTimer.schedule(mUpdateMessageCountTask, 100, 1000);
            mTimer.schedule(mUpdatePipelineStatusTask, 100, 1000);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
            OpenXcEnablerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mServiceNotRunningWarningView.setVisibility(View.VISIBLE);
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
        startService(new Intent(this, PreferenceManagerService.class));

        mServiceNotRunningWarningView = findViewById(R.id.service_not_running_bar);
        mMessageCountView = (TextView) findViewById(R.id.message_count);
        mBluetoothConnIV = findViewById(R.id.connection_bluetooth);
        mUsbConnIV = findViewById(R.id.connection_usb);
        mFileConnIV = findViewById(R.id.connection_file);
        mNetworkConnIV = findViewById(R.id.connection_network);
        mUnknownConnIV = findViewById(R.id.connection_unknown);
        mNoneConnView = findViewById(R.id.connection_none);

        findViewById(R.id.view_vehicle_data_btn).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(OpenXcEnablerActivity.this,
                        VehicleDashboardActivity.class));
            }
        });

        OpenXcEnablerActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mServiceNotRunningWarningView.setVisibility(View.VISIBLE);
            }
        });

        processCrittercismInitDialog();
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

        Log.d(TAG, "Destroying Enabler activity");
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

    /**
     * Process Crittercism. If user previously selects YES - initialize. If NO - skip. If none of above
     * provide a dialog.
     */
    private void processCrittercismInitDialog() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int crittercismPreferenceValue = preferences.getInt(AppConst.CRITTERCISM_DIALOG_DECISION_KEY,
                AppConst.CRITTERCISM_DIALOG_DECISION_DEFAULT);

        if (crittercismPreferenceValue == AppConst.CRITTERCISM_DIALOG_DECISION_NO) {
            return;
        }
        if (crittercismPreferenceValue == AppConst.CRITTERCISM_DIALOG_DECISION_YES) {
            String key = AppUtils.getCrittercismKey(this);
            if (key.isEmpty()) {
                return;
            }
            Crittercism.initialize(getApplicationContext(), key);
            return;
        }
        int appVersionCode = AppUtils.getAppVersionCode(this);
        if (appVersionCode < AppConst.CRITTERCISM_INIT_VERSION_CODE) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(AppConst.CRITTERCISM_DIALOG_DECISION_KEY, AppConst.CRITTERCISM_DIALOG_DECISION_YES);
                editor.commit();

                String key = AppUtils.getCrittercismKey(OpenXcEnablerActivity.this);
                if (key.isEmpty()) {
                    return;
                }
                Crittercism.initialize(getApplicationContext(), key);
                return;
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(AppConst.CRITTERCISM_DIALOG_DECISION_KEY, AppConst.CRITTERCISM_DIALOG_DECISION_NO);
                editor.commit();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.crittercism_dialog_title));
        dialog.setMessage(getString(R.string.crittercism_dialog_message));
        dialog.show();
    }
}