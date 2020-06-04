package com.openxc.enabler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.ListFragment;
import com.openxc.VehicleManager;
import com.openxc.messages.CanMessage;
import com.openxc.messages.VehicleMessage;
import com.openxcplatform.enabler.R;

public class CanMessageViewFragment extends ListFragment {
    private final static String CanMessage = "CanMessageView";

    private VehicleManager mVehicleManager;
    private CanMessageAdapter canMessageAdapter;

    private VehicleMessage.Listener mListener = new VehicleMessage.Listener() {
        @Override
        public void receive(final VehicleMessage message) {
            Activity activity = getActivity();
            if(activity != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        canMessageAdapter.add(message.asCanMessage());
                    }
                });
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(CanMessage, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            mVehicleManager.addListener(CanMessage.class, mListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(CanMessage, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canMessageAdapter = new CanMessageAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.can_message_list_fragment,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(canMessageAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getActivity().bindService(
                    new Intent(getActivity(), VehicleManager.class),
                    mConnection, Context.BIND_AUTO_CREATE);
        } else {
            if(mVehicleManager != null) {
                Log.i(CanMessage, "Unbinding from vehicle service");
                mVehicleManager.removeListener(CanMessage.class, mListener);
                getActivity().unbindService(mConnection);
                mVehicleManager = null;
            }
        }
    }
    @Override
    public void onListItemClick(ListView listView, View view,
            int position, long id) {
        Intent intent = new Intent(getActivity(),
                CanMessageDetailActivity.class);
        intent.putExtra(CanMessageDetailActivity.EXTRA_CAN_MESSAGE,
                canMessageAdapter.getItem(position));
        // This activity is not very useful or performant right now so it isn't
        // enabled - see https://github.com/openxc/openxc-android/issues/159
    }
}
