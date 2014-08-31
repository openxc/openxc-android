package com.openxc.enabler;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.messages.CanMessage;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.ByteAdapter;

public class SendCanMessageFragment extends Fragment {
    private static String TAG = "SendCanMessageFragment";

    private VehicleManager mVehicleManager;
    private View mLastMessageView;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.send_can_message_fragment,
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
                onSendCanMessage(spinner,
                        (EditText) v.findViewById(R.id.message_id),
                        (EditText) v.findViewById(R.id.message_payload));
            }
        });

        mLastMessageView = v.findViewById(R.id.last_message);
        return v;
    }

    private void onSendCanMessage(Spinner busSpinner,
            EditText idView, EditText payloadView) {
        boolean validInput = true;
        if(idView.getText().toString().isEmpty()) {
            idView.setError("ID is required");
            validInput = false;
        }

        if(payloadView.getText().toString().isEmpty()) {
            payloadView.setError("Payload is required");
            validInput = false;
        }

        if(payloadView.getText().toString().length() % 2 == 1) {
            payloadView.setError("Payload must be specified as full bytes");
            validInput = false;
        }

        if(validInput) {
            System.out.println(payloadView.getText().toString());
            CanMessage message = new CanMessage(
                    Integer.valueOf(busSpinner.getSelectedItem().toString()),
                    Integer.valueOf(idView.getText().toString(), 16),
                    ByteAdapter.hexStringToByteArray(payloadView.getText().toString()));
            mVehicleManager.send(message);
            // Make sure to update after sending so the timestamp is set by the
            // VehicleManager
            updateLastMessage(message);
        } else {
            Log.i(TAG, "Form is invalid, not sending message");
        }
    }

    private void updateLastMessage(final CanMessage message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                // TODO This is duplicated in CanMessageAdapter - figure
                // out the best way to share this rendering info
                TextView timestampView = (TextView)
                        mLastMessageView.findViewById(R.id.timestamp);
                timestampView.setText(new SimpleDateFormat("HH:mm:ss.S").format(
                            message.getDate()));

                TextView busView = (TextView)
                        mLastMessageView.findViewById(R.id.bus);
                busView.setText("" + message.getBusId());

                TextView idView = (TextView)
                        mLastMessageView.findViewById(R.id.id);
                idView.setText("0x" + Integer.toHexString(message.getId()));

                TextView payloadView = (TextView) mLastMessageView.findViewById(
                        R.id.data);
                System.out.println(message.getData());
                if(message.getData() != null) {
                    payloadView.setText("0x" + ByteAdapter.byteArrayToHexString(
                                message.getData()));
                }
            }
        });
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
