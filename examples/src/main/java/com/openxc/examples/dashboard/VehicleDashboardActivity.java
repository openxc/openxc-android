package com.openxc.examples.dashboard;

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

import com.openxc.VehicleService;
import com.openxc.examples.R;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.NoValueException;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.WindshieldWiperSpeed;
import com.openxc.remote.RemoteVehicleServiceException;

public class VehicleDashboardActivity extends Activity {

    private static String TAG = "VehicleDashboard";

    private VehicleService mVehicleService;
    private boolean mIsBound;
    private final Handler mHandler = new Handler();
    private TextView mSteeringWheelAngleView;
    private TextView mVehicleSpeedView;
    private TextView mVehicleBrakeStatusView;
    private TextView mParkingBrakeStatusView;
    private TextView mVehicleEngineSpeedView;
    private TextView mTransmissionGearPosView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mVehButtonEventView;
    private TextView mWiperSpeedView;
    StringBuffer mBuffer;

    WindshieldWiperSpeed.Listener mWiperListener =
            new WindshieldWiperSpeed.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final WindshieldWiperSpeed wiperSpeed =
                (WindshieldWiperSpeed) measurement;
            if(!wiperSpeed.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            if(wiperSpeed.getValue().doubleValue() > 0) {
                                mWiperSpeedView.setText("On");
                            } else {
                                mWiperSpeedView.setText("Off");
                            }
                        } catch(NoValueException e) { }
                    }
                });
            }
        }
    };

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

    BrakePedalStatus.Listener mBrPedalStatus = new BrakePedalStatus.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final BrakePedalStatus status = (BrakePedalStatus) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mVehicleBrakeStatusView.setText(
                                "" + status.getValue().booleanValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };
 
    ParkingBrakeStatus.Listener mParkingBrakeStatus = new ParkingBrakeStatus.Listener() {
    	public void receive(VehicleMeasurement measurement) {
    		final ParkingBrakeStatus status = (ParkingBrakeStatus) measurement;
    		if(!status.isNone()) {
    			mHandler.post(new Runnable() {
    				public void run() {
    					try{
    						mParkingBrakeStatusView.setText(
    							"" + status.getValue().booleanValue());
    					} catch(NoValueException e) {}
    				}
    			});
    		}	
    	}
    };
    
    EngineSpeed.Listener mEngineSpeed = new EngineSpeed.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final EngineSpeed status = (EngineSpeed) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mVehicleEngineSpeedView.setText(
                                "" + status.getValue().doubleValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };

    TransmissionGearPosition.Listener mTransmissionGearPos = new TransmissionGearPosition.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final TransmissionGearPosition status = (TransmissionGearPosition) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mTransmissionGearPosView.setText(
                                "" + status.getValue().enumValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };

    VehicleButtonEvent.Listener mVehButtonEvent = new VehicleButtonEvent.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final VehicleButtonEvent event = (VehicleButtonEvent) measurement;
            if(!event.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mVehButtonEventView.setText(
                                event.getValue().enumValue() + " is " +
                                event.getAction().enumValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };


    Latitude.Listener mLatitude =
            new Latitude.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final Latitude lat = (Latitude) measurement;
            if(!lat.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mLatitudeView.setText(
                                "" + lat.getValue().doubleValue());
                        } catch(NoValueException e) { }
                    }
                });
            }
        }
    };

    Longitude.Listener mLongitude =
            new Longitude.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final Longitude lng = (Longitude) measurement;
            if(!lng.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mLongitudeView.setText(
                                "" + lng.getValue().doubleValue());
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
                mVehicleService.addListener(WindshieldWiperSpeed.class,
                        mWiperListener);
                mVehicleService.addListener(BrakePedalStatus.class,
                        mBrPedalStatus);
                mVehicleService.addListener(EngineSpeed.class,
                        mEngineSpeed);
                mVehicleService.addListener(TransmissionGearPosition.class,
                        mTransmissionGearPos);
                mVehicleService.addListener(Latitude.class,
                        mLatitude);
                mVehicleService.addListener(Longitude.class,
                        mLongitude);
                mVehicleService.addListener(VehicleButtonEvent.class,
                        mVehButtonEvent);
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
        mWiperSpeedView = (TextView) findViewById(
                R.id.wiper_speed);
        mVehicleBrakeStatusView = (TextView) findViewById(
        		R.id.brake_pedal_status);
        mParkingBrakeStatusView = (TextView) findViewById(
        		R.id.parking_brake_status);
        mVehicleEngineSpeedView = (TextView) findViewById(
                R.id.engine_speed);
        mTransmissionGearPosView = (TextView) findViewById(
                R.id.transmission_gear_pos);
        mLatitudeView = (TextView) findViewById(
                R.id.latitude);
        mLongitudeView = (TextView) findViewById(
                R.id.longitude);
        mVehButtonEventView = (TextView) findViewById(
                R.id.vehButtonEvent);
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
