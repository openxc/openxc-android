package com.openxc.enabler;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.openxc.VehicleManager;
import com.openxc.enabler.preferences.PreferenceManagerService;
import com.openxc.interfaces.bluetooth.BluetoothException;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.bluetooth.DeviceManager;

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

    /**
     * @return true if the Crashlytics API key is declared in AndroidManifest.xml metadata, otherwise return false.
     */
    static boolean hasCrashlyticsApiKey(Context context) {
        try {
            Context appContext = context.getApplicationContext();
            ApplicationInfo ai = appContext.getPackageManager().getApplicationInfo(
                    appContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return (bundle != null) && (bundle.getString("com.crashlytics.ApiKey") != null);
        } catch (NameNotFoundException e) {
            // Should not happen since the name was determined dynamically from the app context.
            Log.e(TAG, "Unexpected NameNotFound.", e);
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasCrashlyticsApiKey(this)) {
            Crashlytics.start(this);
        } else {
            Log.e(TAG, "No Crashlytics API key found. Visit http://crashlytics.com to set up an account.");

        }

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

        findViewById(R.id.start_bluetooth_search_btn).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    DeviceManager deviceManager = new DeviceManager(OpenXcEnablerActivity.this);
                    deviceManager.startDiscovery();
                    // Re-adding the interface with a null address triggers
                    // automatic mode 1 time
                    mVehicleManager.addVehicleInterface(
                            BluetoothVehicleInterface.class, null);

                    // clears the existing explicitly set Bluetooth device.
                    SharedPreferences.Editor editor =
                            PreferenceManager.getDefaultSharedPreferences(
                                OpenXcEnablerActivity.this).edit();
                    editor.putString(getString(R.string.bluetooth_mac_key),
                            getString(R.string.bluetooth_mac_automatic_option));
                    editor.commit();
                } catch(BluetoothException e) {
                    Toast.makeText(OpenXcEnablerActivity.this,
                        "Bluetooth is disabled, can't search for devices",
                        Toast.LENGTH_LONG).show();
                }
            }
        });

        OpenXcEnablerActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mServiceNotRunningWarningView.setVisibility(View.VISIBLE);
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
}
