package com.openxc.enabler.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.VehicleManager;

public class PreferenceManagerService extends Service {
    private static String TAG = "PreferenceManagerService";

    private IBinder mBinder = new PreferenceBinder();
    private VehicleManager mVehicleManager;
    private BluetoothPreferenceManager mBluetoothPreferenceManager;

    private List<VehiclePreferenceManager> mPreferenceManagers =
            new ArrayList<VehiclePreferenceManager>();

    public class PreferenceBinder extends Binder {
        public PreferenceManagerService getService() {
            return PreferenceManagerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");

        bindService(new Intent(this, VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);

        mPreferenceManagers = new ArrayList<VehiclePreferenceManager>();
        mBluetoothPreferenceManager = new BluetoothPreferenceManager(this);
        mPreferenceManagers.add(mBluetoothPreferenceManager);
        mPreferenceManagers.add(new FileRecordingPreferenceManager(this));
        mPreferenceManagers.add(new GpsOverwritePreferenceManager(this));
        mPreferenceManagers.add(new NativeGpsPreferenceManager(this));
        mPreferenceManagers.add(new UploadingPreferenceManager(this));
        mPreferenceManagers.add(new NetworkPreferenceManager(this));
        mPreferenceManagers.add(new TraceSourcePreferenceManager(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        for(VehiclePreferenceManager manager : mPreferenceManagers) {
            manager.close();
        }

        unbindService(mConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        return mBinder;
    }

    public Map<String, String> getBluetoothDevices() {
        return mBluetoothPreferenceManager.getDiscoveredDevices();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).toString();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            new Thread(new Runnable() {
                public void run() {
                    mVehicleManager.waitUntilBound();
                    for(VehiclePreferenceManager manager : mPreferenceManagers) {
                        manager.setVehicleManager(mVehicleManager);
                    }
                }
            }).start();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };
}
