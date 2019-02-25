package com.openxc.enabler;

import java.util.TimerTask;

import android.app.Activity;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.remote.VehicleServiceException;

public class BitRateCalcTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private TextView mViDeviceThroughputBytesView;

    public BitRateCalcTask(VehicleManager vehicleService, Activity activity,
                            TextView view) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mViDeviceThroughputBytesView = view;
    }

    public void run() {
        int bitrate;

        try {
            bitrate = mVehicleManager.getBitRate();
        } catch(VehicleServiceException e) {
            bitrate = 0;
        }

        final String messageText = Integer.toString(bitrate);
        if(mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    mViDeviceThroughputBytesView.setText(messageText);
                }
            });
        }
    }
}
