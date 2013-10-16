package com.openxc.enabler;

import java.util.TimerTask;

import com.openxc.VehicleManager;
import com.openxc.remote.VehicleServiceException;

import android.app.Activity;
import android.widget.TextView;

public class MessageCountTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private TextView mMessageCountView;

    public MessageCountTask(VehicleManager vehicleService, Activity activity,
            TextView view) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mMessageCountView = view;
    }

    public void run() {
        int messageCount;
        try {
            messageCount = mVehicleManager.getMessageCount();
        } catch(VehicleServiceException e) {
            messageCount = 0;
        }

        final String messageText = Integer.toString(messageCount);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mMessageCountView.setText(messageText);
            }
        });
    }
}
