package com.openxc.enabler;

import android.app.Activity;
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

import com.openxc.VehicleManager;

import com.openxc.messages.SimpleModemMessage;
import com.openxc.messages.VehicleMessage;
import com.openxcplatform.enabler.R;

public class ModemDiagnosticsFragment extends ListFragment {
    private static String TAG = "ModemDiagnostics";

    private VehicleManager mVehicleManager;
    private SimpleModemMessageAdapter mAdapter;
    boolean isV2X;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    
    		super.onCreate(savedInstanceState);
    		mAdapter = new SimpleModemMessageAdapter(getActivity());
    		
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    		View v = inflater.inflate(R.layout.modem_diagnostics_fragment, container, false);
    	
    	
        return v;
    	
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
                Log.i(TAG, "Unbinding from vehicle service");
                
                mVehicleManager.disableModemInterfaceDiagnostics();
                
                mVehicleManager.removeListener(SimpleModemMessage.class, mListener);
                getActivity().unbindService(mConnection);
                mVehicleManager = null;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    
    		 super.onActivityCreated(savedInstanceState);
    	     setListAdapter(mAdapter);
    
    }
    
    public interface FragInterface{
    	public void needsHide();
    }
    
    private VehicleMessage.Listener mListener = new VehicleMessage.Listener() {
        @Override
        public void receive(final VehicleMessage message) {
       
        		 Activity activity = getActivity();
                 if(activity != null) {
                     getActivity().runOnUiThread(new Runnable() {
                         public void run() {
                             mAdapter.add(message.asSimpleModemMessage());
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

                 mVehicleManager.addListener(SimpleModemMessage.class, mListener);
                 
                 mVehicleManager.enableModemInterfaceDiagnostics();
        
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };
}
