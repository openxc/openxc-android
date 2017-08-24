package com.openxc.enabler;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleServiceException;
import com.openxc.remote.ViConnectionListener;
import com.openxcplatform.enabler.R;

import java.util.Date;

public class SendCommandMessageFragment extends Fragment {
    private static String TAG = "SendCommandMsgFragment";

    public static final int SELECT_COMMAND = 0;
    public static final int VERSION_POS = 1;
    public static final int DEVICE_ID_POS = 2;
    public static final int PLATFORM_POS = 3;
    public static final int PASSTHROUGH_CAN_POS = 4;
    public static final int ACCEPTANCE_BYPASS_POS = 5;
    public static final int PAYLOAD_FORMAT_POS = 6;
    public static final int C5_RTC_CONFIG_POS = 7;
    public static final int C5_SD_CARD_POS = 8;
    public static final int CUSTOM_COMMAND_POS = 9;

    private TextView commandResponseTextView;

    private View mServiceNotRunningWarningView;

    private VehicleManager mVehicleManager;

    private View mLastRequestView;
    private LinearLayout mBusLayout;
    private LinearLayout mEnabledLayout;
    private LinearLayout mBypassLayout;
    private LinearLayout mFormatLayout;
    private LinearLayout mCustomInputLayout;

    private Spinner mBusSpinner;
    private Spinner mEnabledSpinner;
    private Spinner mBypassSpinner;
    private Spinner mFormatSpinner;
    private Button mSendButton;
    private EditText mCustomInput;

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

    private ViConnectionListener mConnectionListener = new ViConnectionListener.Stub() {
        public void onConnected(final VehicleInterfaceDescriptor descriptor) {
            Log.d(TAG, descriptor + " is now connected");
        }

        public void onDisconnected() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "VI disconnected");
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
        View v = inflater.inflate(R.layout.send_command_message_fragment, container, false);

        commandResponseTextView = (TextView) v.findViewById(R.id.command_response);
        mServiceNotRunningWarningView = v.findViewById(R.id.service_not_running_bar);
        mBusLayout = (LinearLayout) v.findViewById(R.id.bus_layout);
        mEnabledLayout = (LinearLayout) v.findViewById(R.id.enabled_layout);
        mBypassLayout = (LinearLayout) v.findViewById(R.id.bypass_layout);
        mFormatLayout = (LinearLayout) v.findViewById(R.id.format_layout);
        mLastRequestView = v.findViewById(R.id.last_request);
        mCustomInputLayout = (LinearLayout) v.findViewById(R.id.custom_input_layout);

        mBusSpinner = (Spinner) v.findViewById(R.id.bus_spinner);
        ArrayAdapter<CharSequence> busAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.buses_array
                , android.R.layout.simple_spinner_item);

        busAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mBusSpinner.setAdapter(busAdapter);

        mEnabledSpinner = (Spinner) v.findViewById(R.id.enabled_spinner);
        ArrayAdapter<CharSequence> enabledAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.boolean_array
                , android.R.layout.simple_spinner_item);

        enabledAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mEnabledSpinner.setAdapter(enabledAdapter);

        mBypassSpinner = (Spinner) v.findViewById(R.id.bypass_spinner);
        ArrayAdapter<CharSequence> bypassAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.boolean_array
                , android.R.layout.simple_spinner_item);

        bypassAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mBypassSpinner.setAdapter(bypassAdapter);

        mFormatSpinner = (Spinner) v.findViewById(R.id.format_spinner);
        ArrayAdapter<CharSequence> formatAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.format_array
                , android.R.layout.simple_spinner_item);

        formatAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mFormatSpinner.setAdapter(formatAdapter);

        mCustomInput = (EditText) v.findViewById(R.id.custom_input_id);

        final Spinner commandSpinner = (Spinner) v.findViewById(R.id.command_spinner);
        //set default selection as Select Command
        commandSpinner.setSelection(0);

        final ArrayAdapter<CharSequence> commandAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.commands_array
                , android.R.layout.simple_spinner_item);

        commandAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        commandSpinner.setAdapter(commandAdapter);


        commandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showSelectedCommandView(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        mSendButton = (Button) v.findViewById(R.id.send_request);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedItem = commandSpinner.getSelectedItemPosition();
                sendRequest(selectedItem);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mServiceNotRunningWarningView.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }

    private void sendRequest(int selectedItem) {
        if (mVehicleManager != null) {
            Command request = null;
            VehicleMessage response ;
            int selectedBus;
            Boolean enabled, bypass;
            String format;
            switch (selectedItem) {
                case VERSION_POS:
                    request = new Command(Command.CommandType.VERSION);
                    break;

                case DEVICE_ID_POS:
                    request = new Command(Command.CommandType.DEVICE_ID);
                    break;

                case PLATFORM_POS:
                    request = new Command(Command.CommandType.PLATFORM);
                    break;

                case PASSTHROUGH_CAN_POS:
                    selectedBus = Integer.valueOf(mBusSpinner.getSelectedItem().toString());
                    enabled = Boolean.valueOf(
                            mEnabledSpinner.getSelectedItem().toString());
                    request = new Command(
                            Command.CommandType.PASSTHROUGH, selectedBus, enabled);
                    break;

                case ACCEPTANCE_BYPASS_POS:
                    selectedBus = Integer.valueOf(mBusSpinner.getSelectedItem().toString());
                    bypass = Boolean.valueOf(
                            mBypassSpinner.getSelectedItem().toString());
                    request = new Command(Command.CommandType.AF_BYPASS,bypass, selectedBus);
                    break;

                case PAYLOAD_FORMAT_POS:
                    format = mFormatSpinner.getSelectedItem().toString();
                    request = new Command(format, Command.CommandType.PAYLOAD_FORMAT);
                    break;

                case C5_RTC_CONFIG_POS:
                    request = new Command(Command.CommandType.RTC_CONFIGURATION, new Date().getTime());
                    break;

                case C5_SD_CARD_POS:
                    request = new Command(Command.CommandType.SD_MOUNT_STATUS);
                    break;

                case CUSTOM_COMMAND_POS:
                    String customCommand =  mCustomInput.getText().toString();
                    //TODO: Custom Command needs to be implemented. Temp calling Version Code
                    request = new Command(Command.CommandType.VERSION);
                    break;
                default:
                    break;
            }
            response = mVehicleManager.request(request);
            updateLastRequestView(request);

            commandResponseTextView.setVisibility(View.VISIBLE);
            commandResponseTextView.setText(getCommandResponse(response));

        }
    }

    private void updateLastRequestView(final Command requestMessage) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                TextView timestampView = (TextView)
                        mLastRequestView.findViewById(R.id.timestamp);
                timestampView.setText(VehicleMessageAdapter.formatTimestamp(
                        requestMessage));

                TextView commandView = (TextView)
                        mLastRequestView.findViewById(R.id.command);
                commandView.setText("" + requestMessage.getCommand());

                TextView busView = (TextView)
                        mLastRequestView.findViewById(R.id.bus);
                busView.setText("" + requestMessage.getBus());

                TextView enabledView = (TextView)
                        mLastRequestView.findViewById(R.id.enabled);
                enabledView.setText("" + requestMessage.isEnabled());

                TextView bypassView = (TextView)
                        mLastRequestView.findViewById(R.id.bypass);
                bypassView.setText("" + requestMessage.isBypass());

                TextView formatView = (TextView)
                        mLastRequestView.findViewById(R.id.format);
                formatView.setText("" + requestMessage.getFormat());

            }
        });
    }

    private String getCommandResponse(VehicleMessage vehicleMessage) {
        String acceptanceResponse = null;

        if (vehicleMessage != null) {
            try {
                CommandResponse response = vehicleMessage.asCommandResponse();
                acceptanceResponse = response.toString();
            } catch (ClassCastException e) {
                Log.w(TAG, "Expected a command response but got " + vehicleMessage +
                        " -- ignoring, assuming no response");
            }
        }
        return acceptanceResponse;
    }

    private void showSelectedCommandView(int pos) {
        commandResponseTextView.setVisibility(View.GONE);
        mBusLayout.setVisibility(View.GONE);
        mEnabledLayout.setVisibility(View.GONE);
        mBypassLayout.setVisibility(View.GONE);
        mFormatLayout.setVisibility(View.GONE);
        mCustomInputLayout.setVisibility(View.GONE);
        /*Send button is visible in all views*/
        mSendButton.setVisibility(View.VISIBLE);
        switch (pos) {
            case SELECT_COMMAND:
                mSendButton.setVisibility(View.GONE);
                break;
            case PASSTHROUGH_CAN_POS:
                mBusLayout.setVisibility(View.VISIBLE);
                mEnabledLayout.setVisibility(View.VISIBLE);
                break;
            case ACCEPTANCE_BYPASS_POS:
                mBusLayout.setVisibility(View.VISIBLE);
                mBypassLayout.setVisibility(View.VISIBLE);
                break;
            case PAYLOAD_FORMAT_POS:
                mFormatLayout.setVisibility(View.VISIBLE);
                break;
            case CUSTOM_COMMAND_POS:
                mCustomInputLayout.setVisibility(View.VISIBLE);
                break;
            default: // do nothing
                break;
        }
    }
}
