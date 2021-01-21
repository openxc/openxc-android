package com.openxc.enabler;

import android.annotation.SuppressLint;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.VehicleMessage;
import com.openxcplatform.enabler.R;

import java.util.Timer;
import java.util.TimerTask;

import androidx.fragment.app.ListFragment;

import static com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread;

public class DTCRequestFragment extends ListFragment {
    private final static String DTCMessage = "DTCRequestFragment";

    private VehicleManager mVehicleManager;
    private Button searchBtn;
    private TextView noResponse;
    private boolean displayNoResponse = true;
    private boolean scanComplete = false;
    private ProgressBar progressBar;
    private int progressBarValue = 0;
    private DiagnosticResponseAdapter diagnosticResponseAdapter;

    private VehicleMessage.Listener mListener = new VehicleMessage.Listener() {
        @Override
        public void receive(final VehicleMessage message) {
            if (!((OpenXcEnablerActivity)getActivity()).isDTCScanning()) {
                return;
            }

            Activity activity = getActivity();
            if(activity != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        // responses are collected here
                        Log.e(DTCMessage, "response received");
                        diagnosticResponseAdapter.add(message.asDiagnosticResponse());
                        displayNoResponse = false;
                    }
                });
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e(DTCMessage, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
            ).getService();

            mVehicleManager.addListener(DiagnosticResponse.class, mListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(DTCMessage, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        diagnosticResponseAdapter = new DiagnosticResponseAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.dtc_request_fragment,
                container, false);

        searchBtn = (Button) v.findViewById(R.id.dtc_request_button);
        progressBar = (ProgressBar) v.findViewById(R.id.p_bar);
        //progressBar.setVisibility(View.VISIBLE);
        noResponse = (TextView) v.findViewById(android.R.id.empty);
        noResponse.setVisibility(View.INVISIBLE);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                searchBtn.setEnabled(false);
                searchBtn.setClickable(false);

                Log.e(DTCMessage, "setOnClickListener");
                ManagerThread dtcButtonThread = new ManagerThread();
                dtcButtonThread.start();
            }
        });
        return v;
    }

    public class ManagerThread extends Thread {
        public ManagerThread(){
            Log.e(DTCMessage, "ManagerThread");
        };

        @Override
        public void run() {
            Log.e(DTCMessage, "dtcButtonThread.run");
            onSendDiagnosticRequest();
        }
    };

    private void onSendDiagnosticRequest() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noResponse.setVisibility(View.INVISIBLE);
            }
        });

        ((OpenXcEnablerActivity)getActivity()).setDTCScanning(true);

        //long delay = 420000L;
        long delay = 5000L;
        final Timer scanTimer = new Timer();
        scanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!scanComplete) {
                    progressBar.setProgress(0);
                    scanTimer.cancel();
                    ((OpenXcEnablerActivity)getActivity()).setDTCScanning(false);
                    scanComplete = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            noResponse.setText("The scan took too long and was cancelled");
                            noResponse.setVisibility(View.VISIBLE);
                            searchBtn.setEnabled(true);
                            searchBtn.setClickable(true);
                        }
                    });
                    scanComplete = false;
                }
            }
        }, delay);

        Log.e(DTCMessage, "--------------------------------mVehicleManager = " + mVehicleManager);
        if (mVehicleManager != null) {
            outerLoop:
            for (int a=1; a<=2; a++) {
                for (int b = 0; b <= 2303; b++) {
                    if (!scanComplete) {
                        progressBarValue++;
                        progressBar.setProgress(progressBarValue);
                        DiagnosticRequest request = new DiagnosticRequest(a, b, 3);
                        mVehicleManager.send(request);
                        try {
                            Thread.sleep(20);
                        } catch(InterruptedException e) {
                            Log.e(DTCMessage, "onSendDiagnosticRequest error: " + e);
                        }
                    } else {
                        break outerLoop;
                    }
                }
            }
        }

        ((OpenXcEnablerActivity)getActivity()).setDTCScanning(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (displayNoResponse) {
                    noResponse.setVisibility(View.VISIBLE);
                }
                searchBtn.setEnabled(true);
                searchBtn.setClickable(true);
                scanTimer.cancel();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(diagnosticResponseAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            Log.i(DTCMessage, "Rebinding to vehicle service");
            getActivity().bindService(
                    new Intent(getActivity(), VehicleManager.class),
                    mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            Log.i(DTCMessage, "Unbinding from vehicle service");
            mVehicleManager.removeListener(DiagnosticResponse.class, mListener);
            getActivity().unbindService(mConnection);
            mVehicleManager = null;
        }
    }
}
