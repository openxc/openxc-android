package com.openxc.enabler;

import java.util.TimerTask;

import com.openxc.VehicleManager;
import com.openxc.remote.VehicleServiceException;

import android.os.Handler;
import android.widget.TextView;

public class MessageCountTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Handler mHandler;
    private TextView mMessageCountView;

    public MessageCountTask(VehicleManager vehicleService, Handler handler,
            TextView view) {
        mVehicleManager = vehicleService;
        mHandler = handler;
        mMessageCountView = view;
    }

    public void run() {
        int messageCount;
        try {
            messageCount = mVehicleManager.getMessageCount();
        } catch(VehicleServiceException e) {
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
