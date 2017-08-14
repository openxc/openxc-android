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

public class SendCommandMessageFragment extends Fragment {
    public static final int SELECT_COMMAND = 0;

    public static final int VERSION_POS = 1;
    public static final int DEVICE_ID_POS = 2;
    public static final int PLATFORM_POS = 3;
    public static final int PASSTHROUGH_CAN_POS = 4;
    public static final int ACCEPTANCE_BYPASS_POS = 5;
    public static final int PAYLOAD_FORMAT_POS = 6;
    public static final int C5_RTC_CONFIG_POS = 7;
    public static final int C5_SD_CARD_POS = 8;
    private static String TAG = "SendCommandMsgFragment";

    private TextView commandResponseTextView;

    private View mServiceNotRunningWarningView;

    private VehicleManager mVehicleManager;

    private LinearLayout mBusLayout;
    private LinearLayout mEnabledLayout;
    private LinearLayout mBypassLayout;

    private Spinner mBusSpinner;
    private Spinner mEnabledSpinner;
    private Spinner mBypassSpinner;

    private Button mSendButton;

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
                //TODO: add tasks that you want to do once the VI is connected
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
                runSelectedCommand(i);
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
            switch (selectedItem) {
                case VERSION_POS:
                    sendVersionRequest();
                    break;
                case DEVICE_ID_POS:
                    sendDeviceIdRequest();
                    break;
                case PLATFORM_POS:
                    sendPlatformRequest();
                    break;
                case PASSTHROUGH_CAN_POS:
                    sendPassthroughRequest();
                    break;
                case ACCEPTANCE_BYPASS_POS:
                    sendAcceptanceRequest();
                    break;
                case PAYLOAD_FORMAT_POS:
                    break;
                case C5_RTC_CONFIG_POS:
                    break;
                case C5_SD_CARD_POS:
                    break;
                default:
                    break;
            }
        }
    }

    private void sendPlatformRequest() {
        final String platform = mVehicleManager.getVehicleInterfacePlatform();
        commandResponseTextView.setVisibility(View.VISIBLE);
        commandResponseTextView.setText(platform);

    }

    private void sendDeviceIdRequest() {
        final String deviceId = mVehicleManager.getVehicleInterfaceDeviceId();
        commandResponseTextView.setVisibility(View.VISIBLE);
        commandResponseTextView.setText(deviceId);
    }

    private void sendVersionRequest() {
        final String version = mVehicleManager.getVehicleInterfaceVersion();
        commandResponseTextView.setVisibility(View.VISIBLE);
        commandResponseTextView.setText(version);
    }

    private void sendPassthroughRequest() {
        Command.CommandType passthrough = Command.CommandType.PASSTHROUGH;
        int bus = 1;
        boolean enabled = true;
        String passThroughResponse = getCommandResponse(passthrough, bus, enabled);

        commandResponseTextView.setVisibility(View.VISIBLE);
        commandResponseTextView.setText(passThroughResponse);
    }

    private void sendAcceptanceRequest() {
        Command.CommandType afBypass = Command.CommandType.AF_BYPASS;
        int bus = 1;
        boolean enabled = true;
        String acceptanceResponse = getCommandResponse(afBypass, bus, enabled);

        commandResponseTextView.setVisibility(View.VISIBLE);
        commandResponseTextView.setText(acceptanceResponse);
    }

    private String getCommandResponse(Command.CommandType commandType, int bus, boolean enabled) {
        String acceptanceResponse = null;
        VehicleMessage vehicleMessage = mVehicleManager.request(new Command(commandType, bus, enabled));

        if (vehicleMessage != null) {
            try {
                CommandResponse response = vehicleMessage.asCommandResponse();
                if (response.getStatus()) {
                    acceptanceResponse = response.toString();
                }
            } catch (ClassCastException e) {
                Log.w(TAG, "Expected a command response but got " + vehicleMessage +
                        " -- ignoring, assuming no response");
            }
        }
        return acceptanceResponse;
    }

    private void runSelectedCommand(int pos) {
        commandResponseTextView.setVisibility(View.GONE);
        mBusLayout.setVisibility(View.GONE);
        mEnabledLayout.setVisibility(View.GONE);
        mBypassLayout.setVisibility(View.GONE);
        switch (pos) {
            case SELECT_COMMAND:
                // do nothing as "Select Command" is default selected
                break;
            case VERSION_POS:
                mSendButton.setVisibility(View.VISIBLE);
                break;
            case DEVICE_ID_POS:
                mSendButton.setVisibility(View.VISIBLE);
                break;
            case PLATFORM_POS:
                mSendButton.setVisibility(View.VISIBLE);
                break;
            case PASSTHROUGH_CAN_POS:
                mSendButton.setVisibility(View.VISIBLE);
                showPassthroughView();
                break;
            case ACCEPTANCE_BYPASS_POS:
                mSendButton.setVisibility(View.VISIBLE);
                showAcceptanceView();
                break;
            case PAYLOAD_FORMAT_POS:
                break;
            case C5_RTC_CONFIG_POS:
                break;
            case C5_SD_CARD_POS:
                break;
            default: // do nothing
                break;
        }
    }

    private void showPassthroughView() {
        mBusLayout.setVisibility(View.VISIBLE);
        mEnabledLayout.setVisibility(View.VISIBLE);
    }

    private void showAcceptanceView() {
        mBusLayout.setVisibility(View.VISIBLE);
        mBypassLayout.setVisibility(View.VISIBLE);
    }
}
