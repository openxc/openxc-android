package com.openxc.enabler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.openxc.VehicleManager;
import com.openxc.messages.CanMessage;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.VehicleMessage;

public class CanMessageViewFragment extends ListFragment {
    private static String TAG = "CanMessageView";

    private VehicleManager mVehicleManager;
    private CanMessageAdapter mAdapter;

    private VehicleMessage.Listener mListener = new VehicleMessage.Listener() {
        @Override
        public void receive(final VehicleMessage message) {
            if(message instanceof CanMessage) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mAdapter.add(message.asCanMessage());
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

            // TODO would be nice to be able to register to receive a specific
            // type, sorta like we do with Measurement -
            // addListener(CanMessage.class, listener), for example
            mVehicleManager.addListener(KeyMatcher.getWildcardMatcher(),
                    mListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CanMessageAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.can_message_list_fragment,
                container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().bindService(
                new Intent(getActivity(), VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from vehicle service");
            getActivity().unbindService(mConnection);
            mVehicleManager = null;
        }
    }
}
