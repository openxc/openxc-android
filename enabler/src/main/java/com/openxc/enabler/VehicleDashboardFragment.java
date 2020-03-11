package com.openxc.enabler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.fragment.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.openxc.remote.ViConnectionListener;
import com.openxc.VehicleManager;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxcplatform.enabler.R;

public class VehicleDashboardFragment extends ListFragment {
    private static String TAG = "VehicleDashboard";

    private VehicleManager mVehicleManager;
    private SimpleVehicleMessageAdapter simpleVehicleMessageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleVehicleMessageAdapter = new SimpleVehicleMessageAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vehicle_dashboard, container, false);
        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getActivity() == null) {
            return;
        }

        if (isVisibleToUser) {
            getActivity().bindService(
                    new Intent(getActivity(), VehicleManager.class),
                    mConnection, Context.BIND_AUTO_CREATE);
        } else {
            if(mVehicleManager != null) {
                Log.i(TAG, "Unbinding from vehicle service");
                mVehicleManager.removeListener(SimpleVehicleMessage.class, mListener);
                mVehicleManager.removeListener(EventedSimpleVehicleMessage.class, mListener);
                getActivity().unbindService(mConnection);
                mVehicleManager = null;
            }
        }
    }
    private ViConnectionListener mConnectionListener = new ViConnectionListener.Stub() {
        public void onConnected(final VehicleInterfaceDescriptor descriptor) {
            Log.d(TAG, descriptor + " is now connected");
        }

        public void onDisconnected() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "VI disconnected");
                        disconnectAlert();
                    }
                });
            }
        }
    };
    public  void disconnectAlert() {
        if (PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext()).getBoolean("isPowerDrop", false)) {
            Toast.makeText(getActivity(), "VI Power Droped", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(simpleVehicleMessageAdapter);
    }

    private VehicleMessage.Listener mListener = new VehicleMessage.Listener() {
        @Override
        public void receive(final VehicleMessage message) {
            Activity activity = getActivity();
            if(activity != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if(message instanceof EventedSimpleVehicleMessage) {
                            SimpleVehicleMessage convertedMsg = new SimpleVehicleMessage(message.getTimestamp(),
                                    ((EventedSimpleVehicleMessage) message).getName(),
                                    ((EventedSimpleVehicleMessage) message).getValue() +
                                            ": " + ((EventedSimpleVehicleMessage) message).getEvent());
                            simpleVehicleMessageAdapter.add(convertedMsg.asSimpleMessage());
                        }
                        else
                            simpleVehicleMessageAdapter.add(message.asSimpleMessage());
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

            mVehicleManager.addListener(SimpleVehicleMessage.class, mListener);
            mVehicleManager.addListener(EventedSimpleVehicleMessage.class, mListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };
}
