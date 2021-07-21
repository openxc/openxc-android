package com.openxc.enabler;

import java.util.TimerTask;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.remote.VehicleServiceException;

public class DataThroughputTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private TextView mMessageCountView;
    private TextView mBytesPerSecondView;
    private TextView mMessagesPerSecondView;
    private TextView mAverageMessageSizeView;
    private long connectionStart = (System.currentTimeMillis() / 1000) - 1;

    public DataThroughputTask(VehicleManager vehicleService, Activity activity,
                              TextView messageCountView, TextView bytesPerSecondView, TextView messagesPerSecondView, TextView averageMessageSizeView) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mMessageCountView = messageCountView;
        mBytesPerSecondView = bytesPerSecondView;
        mMessagesPerSecondView = messagesPerSecondView;
        mAverageMessageSizeView = averageMessageSizeView;
    }

    public void run() {
        int messageCount;
        try {
            messageCount = mVehicleManager.getMessageCount();
        } catch(VehicleServiceException e) {
            messageCount = 0;
        }

        int bitrate;
        try {
            bitrate = mVehicleManager.getBitRate();
        } catch(VehicleServiceException e) {
            bitrate = 0;
        }
        int messageRate = (int)(messageCount / ((System.currentTimeMillis() / 1000) - connectionStart ));

        int averageMessageSize = 0;
        if (messageRate > 0)
            averageMessageSize = bitrate / messageRate;

        final String messageCountText = Integer.toString(messageCount);
        final String bytesPerSecondText = Integer.toString(bitrate);
        final String messagesPerSecondText = Integer.toString(messageRate);
        final String averageMessageSizeText = String.format("%d bytes", averageMessageSize);

        Log.d("ThroughputTask", String.format("Thread %d: totalSeconds: %d", Thread.currentThread().getId(), (messageCount / ((System.currentTimeMillis() / 1000) - connectionStart))));

        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    mMessageCountView.setText(messageCountText);
                    mBytesPerSecondView.setText(bytesPerSecondText);
                    mMessagesPerSecondView.setText(messagesPerSecondText);
                    mAverageMessageSizeView.setText(averageMessageSizeText);
                }
            });
        }
    }
}
