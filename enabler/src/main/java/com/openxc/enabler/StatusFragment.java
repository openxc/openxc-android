package com.openxc.enabler;

import java.util.Timer;
import java.util.TimerTask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.interfaces.bluetooth.BluetoothException;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.bluetooth.DeviceManager;
import com.openxc.remote.VehicleServiceException;
import com.openxc.remote.ViConnectionListener;
import com.openxcplatform.enabler.R;

public class StatusFragment extends Fragment {
    private static String TAG = "StatusFragment";

    private TextView mMessageCountView;
    private TextView mViVersionView;
    private TextView mViDeviceIdView;
    private View mBluetoothConnIV;
    private View mUsbConnIV;
    private View mNetworkConnIV;
    private View mFileConnIV;
    private View mNoneConnView;
    private VehicleManager mVehicleManager;
    private View mServiceNotRunningWarningView;

    private TimerTask mUpdateMessageCountTask;
    private TimerTask mUpdatePipelineStatusTask;
    private Timer mTimer;

    private synchronized void updateViInfo() {
        if(mVehicleManager != null) {
            // Must run in another thread or we get circular references to
            // VehicleService -> StatusFragment -> VehicleService and the
            // callback will just fail silently and be removed forever.
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String version = mVehicleManager.getVehicleInterfaceVersion();
                        final String deviceId = mVehicleManager.getVehicleInterfaceDeviceId();
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mViDeviceIdView.setText(deviceId);
                                mViVersionView.setText(version);
                            }
                        });
                    } catch(NullPointerException e) {
                        // A bit of a hack, should probably use a lock - but
                        // this can happen if this view is being paused and it's
                        // not really a problem.
                    }
                }
            }).start();
        }
    }

    private ViConnectionListener mConnectionListener = new ViConnectionListener.Stub() {
        public void onConnected(final VehicleInterfaceDescriptor descriptor) {
            Log.d(TAG, descriptor + " is now connected");
            updateViInfo();
        }

        public void onDisconnected() {
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "VI disconnected");
                        mViVersionView.setText("");
                        mViDeviceIdView.setText("");
                    }
                });
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            try {
                mVehicleManager.addOnVehicleInterfaceConnectedListener(
                        mConnectionListener);
            } catch(VehicleServiceException e) {
                Log.e(TAG, "Unable to register VI connection listener", e);
            }

            if(getActivity() == null) {
                Log.w(TAG, "Status fragment detached from activity");
            }

            if(mVehicleManager.isViConnected()) {
                updateViInfo();
            }

            new Thread(new Runnable() {
                public void run() {
                    try {
                        mVehicleManager.waitUntilBound();
                        if(getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    mServiceNotRunningWarningView.setVisibility(View.GONE);
                                }
                            });
                        }
                    } catch(VehicleServiceException e) {
                        Log.w(TAG, "Unable to connect to VehicleService");
                    }

                }
            }).start();

            mUpdateMessageCountTask = new MessageCountTask(mVehicleManager,
                    getActivity(), mMessageCountView);
            mUpdatePipelineStatusTask = new PipelineStatusUpdateTask(
                    mVehicleManager, getActivity(),
                    mFileConnIV, mNetworkConnIV, mBluetoothConnIV, mUsbConnIV,
                    mNoneConnView);
            mTimer = new Timer();
            mTimer.schedule(mUpdateMessageCountTask, 100, 1000);
            mTimer.schedule(mUpdatePipelineStatusTask, 100, 1000);
        }

        public synchronized void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mServiceNotRunningWarningView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() != null) {
            getActivity().bindService(
                    new Intent(getActivity(), VehicleManager.class),
                    mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            getActivity().unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.status_fragment, container, false);

        mServiceNotRunningWarningView = v.findViewById(R.id.service_not_running_bar);
        mMessageCountView = (TextView) v.findViewById(R.id.message_count);
        mViVersionView = (TextView) v.findViewById(R.id.vi_version);
        mViDeviceIdView = (TextView) v.findViewById(R.id.vi_device_id);
        mBluetoothConnIV = v.findViewById(R.id.connection_bluetooth);
        mUsbConnIV = v.findViewById(R.id.connection_usb);
        mFileConnIV = v.findViewById(R.id.connection_file);
        mNetworkConnIV = v.findViewById(R.id.connection_network);
        mNoneConnView = v.findViewById(R.id.connection_none);

        v.findViewById(R.id.start_bluetooth_search_btn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            DeviceManager deviceManager = new DeviceManager(getActivity());
                            deviceManager.startDiscovery();
                            // Re-adding the interface with a null address triggers
                            // automatic mode 1 time
                            mVehicleManager.setVehicleInterface(
                                    BluetoothVehicleInterface.class, null);

                            // clears the existing explicitly set Bluetooth device.
                            SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(
                                        getActivity()).edit();
                            editor.putString(getString(R.string.bluetooth_mac_key),
                                    getString(R.string.bluetooth_mac_automatic_option));
                            editor.commit();
                        } catch(BluetoothException e) {
                            Toast.makeText(getActivity(),
                                    "Bluetooth is disabled, can't search for devices",
                                    Toast.LENGTH_LONG).show();
                        } catch(VehicleServiceException e) {
                            Log.e(TAG, "Unable to enable Bluetooth vehicle interface", e);
                        }
                    }
                });

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mServiceNotRunningWarningView.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }
}
