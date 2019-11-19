package com.openxc.enabler;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.interfaces.bluetooth.BluetoothException;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.bluetooth.DeviceManager;
import com.openxc.remote.VehicleServiceException;
import com.openxc.remote.ViConnectionListener;
import com.openxc.sources.SourceCallback;
import com.openxcplatform.enabler.R;
import com.openxc.enabler.preferences.FileRecordingPreferenceManager;
import com.openxc.sources.trace.TraceVehicleDataSource;

import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class StatusFragment extends Fragment implements Button.OnClickListener{
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static String TAG = "StatusFragment";

    private TextView mMessageCountView;
    private TextView mViVersionView;
    private TextView mViPlatformView;
    private TextView mViDeviceIdView;
    private View mBluetoothConnIV;
    private View mUsbConnIV;
    private View mNetworkConnIV;
    private View mFileConnIV;
    private View mNoneConnView;
    private VehicleManager mVehicleManager;
    private View mServiceNotRunningWarningView;

    private Button mDisconnect;
    private Button mBluetoothSearch;
    private Button mSplitTraceFile;
    private Button mRestartTraceFile;
    private Button mStartStop;
    private TimerTask mUpdateMessageCountTask;
    private TimerTask mUpdatePipelineStatusTask;
    private Timer mTimer;
    private Context mContext;
    private   FileRecordingPreferenceManager mTracePref;
    private  boolean isTraceRecording;
    private  boolean isDisableTraceLooping;
    private boolean isStart = false;


    private synchronized void updateViInfo() {
        if (mVehicleManager != null) {
            // Must run in another thread or we get circular references to
            // VehicleService -> StatusFragment -> VehicleService and the
            // callback will just fail silently and be removed forever.
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String version = mVehicleManager.getVehicleInterfaceVersion();
                        final String deviceId = mVehicleManager.getVehicleInterfaceDeviceId();
                        final String platform = mVehicleManager.getVehicleInterfacePlatform();
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mViDeviceIdView.setText(deviceId);
                                mViVersionView.setText(version);
                                mViPlatformView.setText(platform);
                            }
                        });
                    } catch (NullPointerException e) {
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
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "VI disconnected");
                        mViVersionView.setText("");
                        mViDeviceIdView.setText("");
                        mViPlatformView.setText("");
                    }
                });
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service
            ).getService();

            try {
                mVehicleManager.addOnVehicleInterfaceConnectedListener(
                        mConnectionListener);
            } catch (VehicleServiceException e) {
                Log.e(TAG, "Unable to register VI connection listener", e);
            }

            if (getActivity() == null) {
                Log.w(TAG, "Status fragment detached from activity");
            }

            if (mVehicleManager.isViConnected()) {
                updateViInfo();
            }

            new Thread(new Runnable() {
                public void run() {
                    try {
                        // It's possible that between starting the thread and
                        // this running, the manager has gone away.
                        if (mVehicleManager != null) {
                            mVehicleManager.waitUntilBound();
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mServiceNotRunningWarningView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    } catch (VehicleServiceException e) {
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
            if (getActivity() != null) {
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
        if (getActivity() != null) {
            getActivity().bindService(
                    new Intent(getActivity(), VehicleManager.class),
                    mConnection, Context.BIND_AUTO_CREATE);
        }

        isDisableTraceLooping = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext()).getBoolean("isDisabledTracePlayingLoop", false);
        isTraceRecording = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext()).getBoolean("IsTraceRecording", false);

        if(isTraceRecording) {
            mSplitTraceFile.setVisibility(View.VISIBLE);
            mStartStop.setVisibility(View.VISIBLE);
        }else{
            mSplitTraceFile.setVisibility(View.GONE);
            mStartStop.setVisibility(View.GONE);
        }
        if (isDisableTraceLooping){
            mDisconnect.setVisibility(View.GONE);
            mRestartTraceFile.setVisibility(View.VISIBLE);
        }else{
            mDisconnect.setVisibility(View.VISIBLE);
            mRestartTraceFile.setVisibility(View.GONE);
        }

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (mVehicleManager != null) {
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
        mViPlatformView = (TextView) v.findViewById(R.id.vi_device_platform);
        mViDeviceIdView = (TextView) v.findViewById(R.id.vi_device_id);
        mBluetoothConnIV = v.findViewById(R.id.connection_bluetooth);
        mUsbConnIV = v.findViewById(R.id.connection_usb);
        mFileConnIV = v.findViewById(R.id.connection_file);
        mNetworkConnIV = v.findViewById(R.id.connection_network);
        mNoneConnView = v.findViewById(R.id.connection_none);
        mBluetoothSearch = v.findViewById(R.id.start_bluetooth_search_btn);
        mBluetoothSearch.setOnClickListener(this);
        mDisconnect = v.findViewById(R.id.disconnect_btn);
        mDisconnect.setOnClickListener(this);
        mSplitTraceFile = v.findViewById(R.id.splittrace_btn);
        mSplitTraceFile.setOnClickListener(this);
        mStartStop = v.findViewById(R.id.starstop_btn);
        mStartStop.setOnClickListener(this);
        mTracePref = new FileRecordingPreferenceManager(requireContext());
        mRestartTraceFile = v.findViewById(R.id.restarttrace_btn);
        mRestartTraceFile.setOnClickListener(this);


        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mServiceNotRunningWarningView.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }

    private void startBluetoothSearch() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (isLocationPermissionGranted() && mBluetoothAdapter.isEnabled()) {
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
                                getActivity().getApplicationContext()).edit();
                editor.putString(getString(R.string.bluetooth_mac_key),
                        getString(R.string.bluetooth_mac_automatic_option));
                editor.commit();
            } catch (BluetoothException e) {
                Toast.makeText(getActivity(),
                        "Bluetooth is disabled, can't search for devices",
                        Toast.LENGTH_LONG).show();
            } catch (VehicleServiceException e) {
                Log.e(TAG, "Unable to enable Bluetooth vehicle interface", e);
            }
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if (!isLocationPermissionGranted()) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(mContext.getApplicationContext(), ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getActivity(), "Bluetooth needs to be enabled for search.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void splitTraceFile(){

        mTracePref.setVehicleManager(mVehicleManager);
        mTracePref.splitTraceFile(true);

    }
    private void startStopClick(){

        if (isStart){
            Log.e(TAG,"clicked Start stop");
            mTracePref.startTraceRecording();
            mStartStop.setText("STOP TRACE");
            isStart=false;
        }else{
            mTracePref.stopTraceRecording();
            mStartStop.setText("START TRACE");
            isStart=true;
        }

    }

    // Restart Tracefile button pressed
    private void restartTraceFile(){
        SourceCallback remoteCallback = mVehicleManager.getRemoteCallback();
        SourceCallback userCallback = mVehicleManager.getUserCallback();
        TraceVehicleDataSource traceVehicleDataSource  = OpenXCApplication.getTraceSource();
        if (traceVehicleDataSource != null) {
            traceVehicleDataSource.startThread(userCallback, mContext);
        } else {
            Toast.makeText(mContext, "No Trace File", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.start_bluetooth_search_btn :
                startBluetoothSearch();
                break;
            case R.id.disconnect_btn :
                try {
                    mVehicleManager.setVehicleInterface(null);
                } catch (VehicleServiceException e) {
                    Log.e(TAG, "Unable to disconnect vehicle interface", e);
                }
                break;
            case R.id.splittrace_btn:
                splitTraceFile();
                break;
            case R.id.starstop_btn:
                startStopClick();
                break;
            case R.id.restarttrace_btn:
                restartTraceFile();
                break;
        }
    }
}
