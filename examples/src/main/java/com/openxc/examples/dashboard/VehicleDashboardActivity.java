package com.openxc.examples.dashboard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.openxc.VehicleService;
import com.openxc.examples.R;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.PowertrainTorque;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.NoValueException;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.WindshieldWiperStatus;
import com.openxc.remote.RemoteVehicleServiceException;

public class VehicleDashboardActivity extends Activity {

    private static String TAG = "VehicleDashboard";

    private VehicleService mVehicleService;
    private boolean mIsBound;
    private final Handler mHandler = new Handler();
    private TextView mSteeringWheelAngleView;
    private TextView mVehicleSpeedView;
    private TextView mFuelConsumedView;
    private TextView mVehicleBrakeStatusView;
    private TextView mParkingBrakeStatusView;
    private TextView mVehicleEngineSpeedView;
    private TextView mPowertrainTorqueView;
    private TextView mAcceleratorPedalPositionView;
    private TextView mTransmissionGearPosView;
    private TextView mIgnitionStatusView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mAndroidLatitudeView;
    private TextView mAndroidLongitudeView;
    private TextView mButtonEventView;
    private TextView mDoorStatusView;
    private TextView mWiperStatusView;
    private TextView mHeadlampStatusView;
    StringBuffer mBuffer;

    WindshieldWiperStatus.Listener mWiperListener =
            new WindshieldWiperStatus.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final WindshieldWiperStatus wiperStatus =
                (WindshieldWiperStatus) measurement;
            if(!wiperStatus.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mWiperStatusView.setText("" +
                                wiperStatus.getValue().booleanValue());
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

    FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final FuelConsumed fuel = (FuelConsumed) measurement;
            if(!fuel.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mFuelConsumedView.setText(
                                "" + fuel.getValue().doubleValue());
                        } catch(NoValueException e) { }
                    }
                });
            }
        }
    };

    BrakePedalStatus.Listener mBrakePedalStatus =
            new BrakePedalStatus.Listener() {
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

    ParkingBrakeStatus.Listener mParkingBrakeStatus =
            new ParkingBrakeStatus.Listener() {
    	public void receive(VehicleMeasurement measurement) {
	    final ParkingBrakeStatus status = (ParkingBrakeStatus) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mParkingBrakeStatusView.setText(
                                "" + status.getValue().booleanValue());
                        } catch(NoValueException e) {}
                    }
                });
             }
        }
    };

    HeadlampStatus.Listener mHeadlampStatus = new HeadlampStatus.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final HeadlampStatus status = (HeadlampStatus) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            mHeadlampStatusView.setText(
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

    PowertrainTorque.Listener mPowertrainTorque =
            new PowertrainTorque.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final PowertrainTorque status = (PowertrainTorque) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mPowertrainTorqueView.setText(
                                "" + status.getValue().doubleValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };

    AcceleratorPedalPosition.Listener mAcceleratorPedalPosition =
            new AcceleratorPedalPosition.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final AcceleratorPedalPosition status =
                (AcceleratorPedalPosition) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mAcceleratorPedalPositionView.setText(
                                "" + status.getValue().doubleValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };


    TransmissionGearPosition.Listener mTransmissionGearPos =
            new TransmissionGearPosition.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final TransmissionGearPosition status =
                    (TransmissionGearPosition) measurement;
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

    IgnitionStatus.Listener mIgnitionStatus =
            new IgnitionStatus.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final IgnitionStatus status = (IgnitionStatus) measurement;
            if(!status.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mIgnitionStatusView.setText(
                                "" + status.getValue().enumValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };

    VehicleButtonEvent.Listener mButtonEvent =
            new VehicleButtonEvent.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final VehicleButtonEvent event = (VehicleButtonEvent) measurement;
            if(!event.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mButtonEventView.setText(
                                event.getValue().enumValue() + " is " +
                                event.getAction().enumValue());
                        } catch(NoValueException e) {}
                    }
                });
            }
        }
    };

    VehicleDoorStatus.Listener mDoorStatus =
            new VehicleDoorStatus.Listener() {
        public void receive(VehicleMeasurement measurement) {
            final VehicleDoorStatus event = (VehicleDoorStatus) measurement;
            if(!event.isNone()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try{
                            mDoorStatusView.setText(
                                event.getValue().enumValue() + " is ajar: " +
                                event.getAction().booleanValue());
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

    LocationListener mAndroidLocationListener = new LocationListener() {
        public void onLocationChanged(final Location location) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mAndroidLatitudeView.setText("" +
                            location.getLatitude());
                        mAndroidLongitudeView.setText("" +
                            location.getLongitude());
                    }
                });
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
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
                mVehicleService.addListener(FuelConsumed.class,
                        mFuelConsumedListener);
                mVehicleService.addListener(WindshieldWiperStatus.class,
                        mWiperListener);
                mVehicleService.addListener(BrakePedalStatus.class,
                        mBrakePedalStatus);
                mVehicleService.addListener(ParkingBrakeStatus.class,
                        mParkingBrakeStatus);
                mVehicleService.addListener(HeadlampStatus.class,
                        mHeadlampStatus);
                mVehicleService.addListener(EngineSpeed.class,
                        mEngineSpeed);
                mVehicleService.addListener(PowertrainTorque.class,
                        mPowertrainTorque);
                mVehicleService.addListener(AcceleratorPedalPosition.class,
                        mAcceleratorPedalPosition);
                mVehicleService.addListener(TransmissionGearPosition.class,
                        mTransmissionGearPos);
                mVehicleService.addListener(IgnitionStatus.class,
                        mIgnitionStatus);
                mVehicleService.addListener(Latitude.class,
                        mLatitude);
                mVehicleService.addListener(Longitude.class,
                        mLongitude);
                mVehicleService.addListener(VehicleButtonEvent.class,
                        mButtonEvent);
                mVehicleService.addListener(VehicleDoorStatus.class,
                        mDoorStatus);
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
        mFuelConsumedView = (TextView) findViewById(
                R.id.fuel_consumed);
        mWiperStatusView = (TextView) findViewById(
                R.id.wiper_status);
        mVehicleBrakeStatusView = (TextView) findViewById(
                R.id.brake_pedal_status);
        mParkingBrakeStatusView = (TextView) findViewById(
                R.id.parking_brake_status);
        mHeadlampStatusView = (TextView) findViewById(
                R.id.headlamp_status);
        mVehicleEngineSpeedView = (TextView) findViewById(
                R.id.engine_speed);
        mPowertrainTorqueView = (TextView) findViewById(
                R.id.powertrain_torque);
        mAcceleratorPedalPositionView = (TextView) findViewById(
                R.id.accelerator_pedal_position);
        mTransmissionGearPosView = (TextView) findViewById(
                R.id.transmission_gear_pos);
        mIgnitionStatusView = (TextView) findViewById(
                R.id.ignition);
        mLatitudeView = (TextView) findViewById(
                R.id.latitude);
        mLongitudeView = (TextView) findViewById(
                R.id.longitude);
        mAndroidLatitudeView = (TextView) findViewById(
                R.id.android_latitude);
        mAndroidLongitudeView = (TextView) findViewById(
                R.id.android_longitude);
        mButtonEventView = (TextView) findViewById(
                R.id.button_event);
        mDoorStatusView = (TextView) findViewById(
                R.id.door_status);
        mBuffer = new StringBuffer();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, VehicleService.class),
                mConnection, Context.BIND_AUTO_CREATE);

        LocationManager locationManager = (LocationManager)
            getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                VehicleService.VEHICLE_LOCATION_PROVIDER, 0, 0,
                mAndroidLocationListener);
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
