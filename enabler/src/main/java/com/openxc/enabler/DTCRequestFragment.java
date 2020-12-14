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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.VehicleMessage;
import com.openxcplatform.enabler.R;

import androidx.fragment.app.ListFragment;

public class DTCRequestFragment extends ListFragment {
    private final static String DTCMessage = "DTCRequestFragment";

    private VehicleManager mVehicleManager;
    private DiagnosticResponseAdapter diagnosticResponseAdapter;
    private View mLastRequestView;

    private VehicleMessage.Listener mListener = new VehicleMessage.Listener() {
        @Override
        public void receive(final VehicleMessage message) {
            Activity activity = getActivity();
            if(activity != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        diagnosticResponseAdapter.add(message.asDiagnosticResponse());
                    }
                });
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(DTCMessage, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
            ).getService();

            mVehicleManager.addListener(DiagnosticResponse.class, mListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(DTCMessage, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    public class ManagerThread extends Thread {
        public ManagerThread(){
            Log.e("DTCRequest", "ManagerThread");
        };

        @Override
        public void run() {
            Log.e("DTCRequest", "dtcButtonThread.run");
            onSendDiagnosticRequest();
        }
    };

    ManagerThread dtcButtonThread = new ManagerThread();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.dtc_request_fragment,
                container, false);

        Button btn = (Button) v.findViewById(R.id.dtc_request_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                Log.e("DTCRequest", "setOnClickListener");
                dtcButtonThread.start();
            }
        });
        return v;
    }

    public class RequestThread extends Thread {
        private int bus;
        private int id;
        private int mode;

        public RequestThread (int bus, int id, int mode) {
            this.bus = bus;
            this.id = id;
            this.mode = mode;
        }

        @Override
        public void run() {
            //DiagnosticRequest(bus, id, mode)
            DiagnosticRequest request = new DiagnosticRequest(bus, id, mode);
            mVehicleManager.send(request);
            // Make sure to update after sending so the timestamp is set by the VehicleManager
            //updateLastRequestView(request);

            Log.e("DTCRequest", "bus = " + bus + ", id = " + id + ", mode = " + mode);
        }
    }

    private void onSendDiagnosticRequest() {

        Toast.makeText(getContext(), "onSendDiagnosticRequest()", Toast.LENGTH_LONG).show();

        for (int a=1; a<=2; a++) {
            for (int b = 0; b <= 2303; b++) {
                RequestThread requestThread = new RequestThread(a, b, 3);
                requestThread.start();
                try {
                    Thread.sleep(20);
                } catch(InterruptedException e) {

                }
            }
        }
    }

    /*
    private final static String DTCMessage = "DTCRequestFragment";

    private VehicleManager mVehicleManager;
    private DiagnosticResponseAdapter diagnosticResponseAdapter;
    private View mLastRequestView;

    private VehicleMessage.Listener mListener = new VehicleMessage.Listener() {
        @Override
        public void receive(final VehicleMessage message) {
            Activity activity = getActivity();
            if(activity != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        diagnosticResponseAdapter.add(message.asDiagnosticResponse());
                    }
                });
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(DTCMessage, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            mVehicleManager.addListener(DiagnosticResponse.class, mListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(DTCMessage, "VehicleService disconnected unexpectedly");
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

        final Spinner spinner = (Spinner) v.findViewById(R.id.bus_spinner);
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.buses_array
                , android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        Button btn = (Button) v.findViewById(R.id.send_request);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                onSendDiagnosticRequest(spinner,
                        (EditText) v.findViewById(R.id.diag_request_id),
                        (EditText) v.findViewById(R.id.diag_request_mode),
                        (EditText) v.findViewById(R.id.diag_request_pid));
            }
        });

        mLastRequestView = v.findViewById(R.id.last_request);
        return v;
    }

    private void onSendDiagnosticRequest(Spinner busSpinner,
            EditText idView, EditText modeView, EditText pidView) {
        boolean validInput = true;
        if(idView.getText().toString().isEmpty()) {
            idView.setError("ID is required");
            validInput = false;
        }

        if(modeView.getText().toString().isEmpty()) {
            modeView.setError("Mode is required");
            validInput = false;
        }

        if(validInput) {
            DiagnosticRequest request = new DiagnosticRequest(
                    Integer.valueOf(busSpinner.getSelectedItem().toString()),
                    Integer.valueOf(idView.getText().toString(), 16),
                    Integer.valueOf(modeView.getText().toString(), 16));
            // Make sure to update after sending so the timestamp is set by the
            // VehicleManager
            String pidString = pidView.getText().toString();
            if(!pidString.isEmpty()) {
                request.setPid(Integer.valueOf(pidString, 16));
            }
            mVehicleManager.send(request);
            // Make sure to update after sending so the timestamp is set by the
            // VehicleManager
            updateLastRequestView(request);
        } else {
            Log.i(DTCMessage, "Form is invalid, not sending diagnostic request");
        }
    }

    private void updateLastRequestView(final DiagnosticRequest request) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                // This is duplicated in DiagnosticResponseAdapter - figure
                // out the best way to share this rendering info
                TextView timestampView = (TextView)
                        mLastRequestView.findViewById(R.id.timestamp);
                timestampView.setText(VehicleMessageAdapter.formatTimestamp(
                            request));

                TextView busView = (TextView)
                        mLastRequestView.findViewById(R.id.bus);
                busView.setText("" + request.getBusId());

                TextView idView = (TextView)
                        mLastRequestView.findViewById(R.id.id);
                idView.setText("0x" + Integer.toHexString(request.getId()));

                TextView modeView = (TextView)
                        mLastRequestView.findViewById(R.id.mode);
                modeView.setText("0x" + Integer.toHexString(request.getMode()));

                if(request.hasPid()) {
                    TextView pidView = (TextView)
                            mLastRequestView.findViewById(R.id.pid);
                    pidView.setText("0x" + Integer.toHexString(
                                request.getPid()));
                }
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
    */
}
