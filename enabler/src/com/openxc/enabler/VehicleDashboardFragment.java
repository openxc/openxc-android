package com.openxc.enabler;

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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.WindshieldWiperStatus;
import com.openxc.remote.VehicleServiceException;

public class VehicleDashboardFragment extends Fragment {

    private static String TAG = "VehicleDashboard";

    private VehicleManager mVehicleManager;
    private final Handler mHandler = new Handler();
    private LocationManager mLocationManager;
    private TextView mSteeringWheelAngleView;
    private TextView mVehicleSpeedView;
    private TextView mFuelConsumedView;
    private TextView mFuelLevelView;
    private TextView mOdometerView;
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
    private TextView mWiperStatusView;
    private TextView mHeadlampStatusView;

    WindshieldWiperStatus.Listener mWiperListener =
            new WindshieldWiperStatus.Listener() {
        public void receive(Measurement measurement) {
            final WindshieldWiperStatus wiperStatus =
                (WindshieldWiperStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mWiperStatusView.setText(wiperStatus.toString());
                }
            });
        }
    };

    VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleSpeedView.setText(speed.toString());
                }
            });
        }
    };

    FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
        public void receive(Measurement measurement) {
            final FuelConsumed fuel = (FuelConsumed) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mFuelConsumedView.setText(fuel.toString());
                }
            });
        }
    };

    FuelLevel.Listener mFuelLevelListener = new FuelLevel.Listener() {
        public void receive(Measurement measurement) {
            final FuelLevel level = (FuelLevel) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mFuelLevelView.setText(level.toString());
                }
            });
        }
    };

    Odometer.Listener mOdometerListener = new Odometer.Listener() {
        public void receive(Measurement measurement) {
            final Odometer odometer = (Odometer) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mOdometerView.setText(odometer.toString());
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
                    mVehicleBrakeStatusView.setText(status.toString());
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
                    mParkingBrakeStatusView.setText(status.toString());
                }
            });
        }
    };

    HeadlampStatus.Listener mHeadlampStatus = new HeadlampStatus.Listener() {
        public void receive(Measurement measurement) {
            final HeadlampStatus status = (HeadlampStatus) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mHeadlampStatusView.setText(status.toString());
                }
            });
        }
    };

    EngineSpeed.Listener mEngineSpeed = new EngineSpeed.Listener() {
        public void receive(Measurement measurement) {
            final EngineSpeed status = (EngineSpeed) measurement;
            mHandler.post(new Runnable() {
                public void run() {
                    mVehicleEngineSpeedView.setText(status.toString());
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
                    mTorqueAtTransmissionView.setText(status.toString());
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
                    mAcceleratorPedalPositionView.setText(status.toString());
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
                    mTransmissionGearPosView.setText(status.toString());
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
                    mIgnitionStatusView.setText(status.toString());
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
                    mLatitudeView.setText(lat.toString());
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
                    mLongitudeView.setText(lng.toString());
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
                    mSteeringWheelAngleView.setText(angle.toString());
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
            } catch(VehicleServiceException e) {
                Log.w(TAG, "Couldn't add listeners for measurements", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.vehicle_dashboard, container, false);

        mLocationManager = (LocationManager)
            getActivity().getSystemService(Context.LOCATION_SERVICE);

        mSteeringWheelAngleView = (TextView) v.findViewById(
                R.id.steering_wheel_angle);
        mVehicleSpeedView = (TextView) v.findViewById(
                R.id.vehicle_speed);
        mFuelConsumedView = (TextView) v.findViewById(
                R.id.fuel_consumed);
        mFuelLevelView = (TextView) v.findViewById(
                R.id.fuel_level);
        mOdometerView = (TextView) v.findViewById(
                R.id.odometer);
        mWiperStatusView = (TextView) v.findViewById(
                R.id.wiper_status);
        mVehicleBrakeStatusView = (TextView) v.findViewById(
                R.id.brake_pedal_status);
        mParkingBrakeStatusView = (TextView) v.findViewById(
                R.id.parking_brake_status);
        mHeadlampStatusView = (TextView) v.findViewById(
                R.id.headlamp_status);
        mVehicleEngineSpeedView = (TextView) v.findViewById(
                R.id.engine_speed);
        mTorqueAtTransmissionView = (TextView) v.findViewById(
                R.id.torque_at_transmission);
        mAcceleratorPedalPositionView = (TextView) v.findViewById(
                R.id.accelerator_pedal_position);
        mTransmissionGearPosView = (TextView) v.findViewById(
                R.id.transmission_gear_pos);
        mIgnitionStatusView = (TextView) v.findViewById(
                R.id.ignition);
        mLatitudeView = (TextView) v.findViewById(
                R.id.latitude);
        mLongitudeView = (TextView) v.findViewById(
                R.id.longitude);
        mAndroidLatitudeView = (TextView) v.findViewById(
                R.id.android_latitude);
        mAndroidLongitudeView = (TextView) v.findViewById(
                R.id.android_longitude);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().bindService(
                new Intent(getActivity(), VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0,
                    mAndroidLocationListener);
        } catch(IllegalArgumentException e) {
            Log.w(TAG, "Vehicle location provider is unavailable");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from vehicle service");
            getActivity().unbindService(mConnection);
            mVehicleManager = null;
        }

        mLocationManager.removeUpdates(mAndroidLocationListener);
    }
}
