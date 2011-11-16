package com.openxc;

import com.openxc.measurements.NoValueException;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;

import com.openxc.remote.RemoteVehicleServiceException;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;

import android.widget.TextView;

public class VehicleDashboardActivity extends Activity {

    private static String TAG = "VehicleDashboard";

    private VehicleService mVehicleService;
    private boolean mIsBound;
    private final Handler mHandler = new Handler();
    private TextView mSteeringWheelAngleView;
    private TextView mVehicleSpeedView;;
    StringBuffer mBuffer;

    VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            if(!speed.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mVehicleSpeedView.setText(
                                "" + speed.getValue().doubleValue());
                        } catch(NoValueException e) { }
                    }
                });
            }
        }
    };

    SteeringWheelAngle.Listener mSteeringWheelListener =
            new SteeringWheelAngle.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
            if(!angle.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mSteeringWheelAngleView.setText(
                                "" + angle.getValue().doubleValue());
                        } catch(NoValueException e) { }
                    }
                });
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleService");
            mVehicleService = ((VehicleService.VehicleServiceBinder)service
                    ).getService();

            try {
                mVehicleService.addListener(SteeringWheelAngle.class,
                        mSteeringWheelListener);
                mVehicleService.addListener(VehicleSpeed.class,
                        mSpeedListener);
            } catch(RemoteVehicleServiceException e) {
                Log.w(TAG, "Couldn't add listeners for measurements", e);
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "Couldn't add listeners for measurements", e);
            }
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "RemoteVehicleService disconnected unexpectedly");
            mVehicleService = null;
            mIsBound = false;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		Log.i(TAG, "Vehicle dashboard created");

        mSteeringWheelAngleView = (TextView) findViewById(
                R.id.steering_wheel_angle);
        mVehicleSpeedView = (TextView) findViewById(
                R.id.vehicle_speed);
        mBuffer = new StringBuffer();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, VehicleService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mIsBound) {
            Log.i(TAG, "Unbinding from vehicle service");
            unbindService(mConnection);
            mIsBound = false;
        }
    }
}
