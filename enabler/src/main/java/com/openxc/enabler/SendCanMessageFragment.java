package com.openxc.enabler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.fragment.app.ListFragment;
import com.openxc.VehicleManager;
import com.openxc.messages.CanMessage;
import com.openxc.messages.formatters.ByteAdapter;
import com.openxcplatform.enabler.R;

public class SendCanMessageFragment extends ListFragment implements TextWatcher  {
    public static final String PAYLOAD_IS_REQUIRED = "Payload is required";
    private static String TAG = "SendCanMessageFragment";

    private VehicleManager mVehicleManager;

    private VehicleMessageAdapter vehicleMessageAdapter;
    private EditText payLoad1, payLoad2, payLoad3, payLoad4, payLoad5, payLoad6, payLoad7, payLoad8;
    private EditText idView;
    private Spinner spinner;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vehicleMessageAdapter = new CanMessageAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.send_can_message_fragment,
                container, false);
        initViews(v);
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

        Button btn =  v.findViewById(R.id.send_request);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                onSendCanMessage();
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(vehicleMessageAdapter);
    }

    private void onSendCanMessage() {
        boolean validInput = true;
        String payLoadValue1 = payLoad1.getText().toString();
        String payLoadValue2 = payLoad2.getText().toString();
        String payLoadValue3 = payLoad3.getText().toString();
        String payLoadValue4 = payLoad4.getText().toString();
        String payLoadValue5 = payLoad5.getText().toString();
        String payLoadValue6 = payLoad6.getText().toString();
        String payLoadValue7 = payLoad7.getText().toString();
        String payLoadValue8 = payLoad8.getText().toString();

        if (idView.getText().toString().isEmpty()) {
            idView.setError("ID is required");
            validInput = false;
        }
        if (payLoadValue1.isEmpty()) {
            payLoad1.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }
        if (payLoadValue2.isEmpty()) {
            payLoad2.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }
        if (payLoadValue3.isEmpty()) {
            payLoad3.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }
        if (payLoadValue4.isEmpty()) {
            payLoad4.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }
        if (payLoadValue5.isEmpty()) {
            payLoad5.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }
        if (payLoadValue6.isEmpty()) {
            payLoad6.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }
        if (payLoadValue7.isEmpty()) {
            payLoad7.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }
        if (payLoadValue8.isEmpty()) {
            payLoad8.setError(PAYLOAD_IS_REQUIRED);
            validInput = false;
        }


        if (validInput) {
            String payloadValue = payLoadValue1 + payLoadValue2 + payLoadValue3 +
                    payLoadValue4 + payLoadValue5 + payLoadValue6 +
                    payLoadValue7 + payLoadValue8;

            if (payloadValue.length() % 2 == 1) {  //payloadView.getText().toString().length()
                payLoad1.setError("Payload must be specified as full bytes");
                validInput = false;
            }

            CanMessage message = new CanMessage(
                    Integer.valueOf(spinner.getSelectedItem().toString()),
                    Integer.valueOf(idView.getText().toString(), 16),
                    ByteAdapter.hexStringToByteArray(payloadValue)); //payloadView.getText().toString()
            mVehicleManager.send(message);
            // Make sure to update after sending so the timestamp is set by the
            // VehicleManager
            vehicleMessageAdapter.add(message.asCanMessage());
        } else {
            Log.i(TAG, "Form is invalid, not sending message");
        }
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
            Log.i(TAG, "Unbinding from vehicle service");
            getActivity().unbindService(mConnection);
            mVehicleManager = null;
        }
    }
    private void initViews(View v) {
        spinner = v.findViewById(R.id.bus_spinner);
        idView = v.findViewById(R.id.message_id);
        payLoad1 = v.findViewById(R.id.message_payload);
        payLoad2 = v.findViewById(R.id.message_payload2);
        payLoad3 = v.findViewById(R.id.message_payload3);
        payLoad4 = v.findViewById(R.id.message_payload4);
        payLoad5 = v.findViewById(R.id.message_payload5);
        payLoad6 = v.findViewById(R.id.message_payload6);
        payLoad7 = v.findViewById(R.id.message_payload7);
        payLoad8 = v.findViewById(R.id.message_payload8);

        payLoad1.addTextChangedListener(this);
        payLoad2.addTextChangedListener(this);
        payLoad3.addTextChangedListener(this);
        payLoad4.addTextChangedListener(this);
        payLoad5.addTextChangedListener(this);
        payLoad6.addTextChangedListener(this);
        payLoad7.addTextChangedListener(this);
        payLoad8.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {


    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 2) {
            payLoadChanged(payLoad1, payLoad2);
            payLoadChanged(payLoad2, payLoad3);
            payLoadChanged(payLoad3, payLoad4);
            payLoadChanged(payLoad4, payLoad5);
            payLoadChanged(payLoad5, payLoad6);
            payLoadChanged(payLoad6, payLoad7);
            payLoadChanged(payLoad7, payLoad8);
            if (getLength(payLoad8) == 2) {
                requestFocusAndCursorVisible(payLoad8);
            }
        }
    }

    private void payLoadChanged(EditText payLoad1, EditText payLoad2) {
        if (getLength(payLoad1) == 2) {
            requestFocusAndCursorVisible(payLoad1, payLoad2);
        }
    }

    private int getLength(EditText payLoad) {
        return payLoad.getText().toString().trim().length();
    }

    private void requestFocusAndCursorVisible(EditText payLoad1, EditText payLoad2) {
        payLoad1.clearFocus();
        requestFocusAndCursorVisible(payLoad2);
    }
        private void requestFocusAndCursorVisible(EditText payLoad) {
        payLoad.requestFocus();
        payLoad.setCursorVisible(true);
    }
}
