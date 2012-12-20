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

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.openxc.measurements.TurnSignalStatus;
import com.openxc.VehicleManager;
import com.openxc.examples.R;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.FineOdometer;
import com.openxc.measurements.WindshieldWiperStatus;
import com.openxc.remote.VehicleServiceException;

public class VehicleDashboardActivity extends Activity {

    private static String TAG = "VehicleDashboard";

    private VehicleManager mVehicleManager;
    private boolean mIsBound;
    private final Handler mHandler = new Handler();
    private TextView mSteeringWheelAngleView;
    private TextView mVehicleSpeedView;
    private TextView mFuelConsumedView;
    private TextView mFuelLevelView;
    private TextView mOdometerView;
    private TextView mFineOdometerView;
    private TextView mVehicleBrakeStatusView;
    private TextView mParkingBrakeStatusView;
    private TextView mVehicleEngineSpeedView;
    private TextView mTorqueAtTransmissionView;
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
        public void receive(Measurement measurement) {
            final WindshieldWiperStatus wiperStatus =
                (WindshieldWiperStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mWiperStatusView.setText("" +
                        wiperStatus.getValue().booleanValue());
                }
            });
        }
    };

    VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleSpeedView.setText(
                        "" + speed.getValue().doubleValue());
                }
            });
        }
    };

    FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
        public void receive(Measurement measurement) {
            final FuelConsumed fuel = (FuelConsumed) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mFuelConsumedView.setText(
                        "" + fuel.getValue().doubleValue());
                }
            });
        }
    };

    FuelLevel.Listener mFuelLevelListener = new FuelLevel.Listener() {
        public void receive(Measurement measurement) {
            final FuelLevel level = (FuelLevel) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mFuelLevelView.setText(
                        "" + level.getValue().doubleValue());
                }
            });
        }
    };

    Odometer.Listener mOdometerListener = new Odometer.Listener() {
        public void receive(Measurement measurement) {
            final Odometer odometer = (Odometer) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mOdometerView.setText(
                        "" + odometer.getValue().doubleValue());
                }
            });
        }
    };

    FineOdometer.Listener mFineOdometerListener = new FineOdometer.Listener() {
        public void receive(Measurement measurement) {
            final FineOdometer odometer = (FineOdometer) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mFineOdometerView.setText(
                        "" + odometer.getValue().doubleValue());
                }
            });
        }
    };

    BrakePedalStatus.Listener mBrakePedalStatus =
            new BrakePedalStatus.Listener() {
        public void receive(Measurement measurement) {
            final BrakePedalStatus status = (BrakePedalStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleBrakeStatusView.setText(
                        "" + status.getValue().booleanValue());
                }
            });
        }
    };

    ParkingBrakeStatus.Listener mParkingBrakeStatus =
            new ParkingBrakeStatus.Listener() {
    	public void receive(Measurement measurement) {
	    final ParkingBrakeStatus status = (ParkingBrakeStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mParkingBrakeStatusView.setText(
                        "" + status.getValue().booleanValue());
                }
            });
        }
    };

    HeadlampStatus.Listener mHeadlampStatus = new HeadlampStatus.Listener() {
        public void receive(Measurement measurement) {
            final HeadlampStatus status = (HeadlampStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mHeadlampStatusView.setText(
                        "" + status.getValue().booleanValue());
                }
            });
        }
    };

    EngineSpeed.Listener mEngineSpeed = new EngineSpeed.Listener() {
        public void receive(Measurement measurement) {
            final EngineSpeed status = (EngineSpeed) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleEngineSpeedView.setText(
                        "" + status.getValue().doubleValue());
                }
            });
        }
    };

    TorqueAtTransmission.Listener mTorqueAtTransmission =
            new TorqueAtTransmission.Listener() {
        public void receive(Measurement measurement) {
            final TorqueAtTransmission status = (TorqueAtTransmission) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mTorqueAtTransmissionView.setText(
                        "" + status.getValue().doubleValue());
                }
            });
        }
    };

    AcceleratorPedalPosition.Listener mAcceleratorPedalPosition =
            new AcceleratorPedalPosition.Listener() {
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition status =
                (AcceleratorPedalPosition) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mAcceleratorPedalPositionView.setText(
                        "" + status.getValue().doubleValue());
                }
            });
        }
    };


    TransmissionGearPosition.Listener mTransmissionGearPos =
            new TransmissionGearPosition.Listener() {
        public void receive(Measurement measurement) {
            final TransmissionGearPosition status =
                    (TransmissionGearPosition) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mTransmissionGearPosView.setText(
                        "" + status.getValue().enumValue());
                }
            });
        }
    };

    IgnitionStatus.Listener mIgnitionStatus =
            new IgnitionStatus.Listener() {
        public void receive(Measurement measurement) {
            final IgnitionStatus status = (IgnitionStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mIgnitionStatusView.setText(
                        "" + status.getValue().enumValue());
                }
            });
        }
    };

    VehicleButtonEvent.Listener mButtonEvent =
            new VehicleButtonEvent.Listener() {
        public void receive(Measurement measurement) {
            final VehicleButtonEvent event = (VehicleButtonEvent) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mButtonEventView.setText(
                        event.getValue().enumValue() + " is " +
                        event.getEvent().enumValue());
                }
            });
        }
    };

    VehicleDoorStatus.Listener mDoorStatus =
            new VehicleDoorStatus.Listener() {
        public void receive(Measurement measurement) {
            final VehicleDoorStatus event = (VehicleDoorStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mDoorStatusView.setText(
                        event.getValue().enumValue() + " is ajar: " +
                        event.getEvent().booleanValue());
                }
            });
        }
    };

    Latitude.Listener mLatitude =
            new Latitude.Listener() {
        public void receive(Measurement measurement) {
            final Latitude lat = (Latitude) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mLatitudeView.setText(
                        "" + lat.getValue().doubleValue());
                }
            });
        }
    };

    Longitude.Listener mLongitude =
            new Longitude.Listener() {
        public void receive(Measurement measurement) {
            final Longitude lng = (Longitude) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mLongitudeView.setText(
                        "" + lng.getValue().doubleValue());
                }
            });
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
        public void receive(Measurement measurement) {
            final SteeringWheelAngle angle = (SteeringWheelAngle) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mSteeringWheelAngleView.setText(
                        "" + angle.getValue().doubleValue());
                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            try {
                mVehicleManager.addListener(SteeringWheelAngle.class,
                        mSteeringWheelListener);
                mVehicleManager.addListener(VehicleSpeed.class,
                        mSpeedListener);
                mVehicleManager.addListener(FuelConsumed.class,
                        mFuelConsumedListener);
                mVehicleManager.addListener(FuelLevel.class,
                        mFuelLevelListener);
                mVehicleManager.addListener(Odometer.class,
                        mOdometerListener);
                mVehicleManager.addListener(FineOdometer.class,
                        mFineOdometerListener);
                mVehicleManager.addListener(WindshieldWiperStatus.class,
                        mWiperListener);
                mVehicleManager.addListener(BrakePedalStatus.class,
                        mBrakePedalStatus);
                mVehicleManager.addListener(ParkingBrakeStatus.class,
                        mParkingBrakeStatus);
                mVehicleManager.addListener(HeadlampStatus.class,
                        mHeadlampStatus);
                mVehicleManager.addListener(EngineSpeed.class,
                        mEngineSpeed);
                mVehicleManager.addListener(TorqueAtTransmission.class,
                        mTorqueAtTransmission);
                mVehicleManager.addListener(AcceleratorPedalPosition.class,
                        mAcceleratorPedalPosition);
                mVehicleManager.addListener(TransmissionGearPosition.class,
                        mTransmissionGearPos);
                mVehicleManager.addListener(IgnitionStatus.class,
                        mIgnitionStatus);
                mVehicleManager.addListener(Latitude.class,
                        mLatitude);
                mVehicleManager.addListener(Longitude.class,
                        mLongitude);
                mVehicleManager.addListener(VehicleButtonEvent.class,
                        mButtonEvent);
                mVehicleManager.addListener(VehicleDoorStatus.class,
                        mDoorStatus);
            } catch(VehicleServiceException e) {
                Log.w(TAG, "Couldn't add listeners for measurements", e);
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "Couldn't add listeners for measurements", e);
            }
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
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
        mFuelLevelView = (TextView) findViewById(
                R.id.fuel_level);
        mOdometerView = (TextView) findViewById(
                R.id.odometer);
        mFineOdometerView = (TextView) findViewById(
                R.id.fine_odometer);
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
        mTorqueAtTransmissionView = (TextView) findViewById(
                R.id.torque_at_transmission);
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
        bindService(new Intent(this, VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);

        LocationManager locationManager = (LocationManager)
            getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(
                    VehicleManager.VEHICLE_LOCATION_PROVIDER, 0, 0,
                    mAndroidLocationListener);
        } catch(IllegalArgumentException e) {
            Log.w(TAG, "Vehicle location provider is unavailable");
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TurnSignalStatus command;
        switch (item.getItemId()) {
        case R.id.left_turn:
            command = new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.LEFT);
            try {
                mVehicleManager.send(command);
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "Unable to send turn signal command", e);
            }
            return true;
        case R.id.right_turn:
            command = new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.RIGHT);
            try {
                mVehicleManager.send(command);
            } catch(UnrecognizedMeasurementTypeException e) {
                Log.w(TAG, "Unable to send turn signal command", e);
            }
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
