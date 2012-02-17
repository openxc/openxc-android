package com.openxc.enabler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;


public class OpenXcEnablerActivity extends Activity {

    private static String TAG = "OpenXcEnablerActivity";

    private VehicleService mVehicleService;
    private final Handler mHandler = new Handler();
    private TextView mVehicleServiceStatusView;;

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

        mVehicleServiceStatusView = (TextView) findViewById(
                R.id.vehicle_service_status);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, VehicleService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }
}
