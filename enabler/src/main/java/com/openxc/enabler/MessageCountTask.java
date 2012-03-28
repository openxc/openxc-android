package com.openxc.enabler;

import java.util.TimerTask;

import com.openxc.VehicleService;
import com.openxc.remote.RemoteVehicleServiceException;

import android.os.Handler;
import android.widget.TextView;

public class MessageCountTask extends TimerTask {
    private final String TAG = "MessageCountTask";

    private VehicleService mVehicleService;
    private Handler mHandler;
    private TextView mMessageCountView;

    public MessageCountTask(VehicleService vehicleService, Handler handler,
            TextView view) {
        mVehicleService = vehicleService;
        mHandler = handler;
        mMessageCountView = view;
    }

    public void run() {
        int messageCount;
        try {
            messageCount = mVehicleService.getMessageCount();
        } catch(RemoteVehicleServiceException e) {
            messageCount = 0;
        }

        final String messageText = messageCount + " messages";
        mHandler.post(new Runnable() {
            public void run() {
                mMessageCountView.setText(messageText);
            }
        });
    }
}
