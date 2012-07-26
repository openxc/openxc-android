package com.openxc.enabler;

import java.util.TimerTask;

import android.app.Activity;

import com.openxc.VehicleManager;
import com.openxc.remote.VehicleServiceException;

import android.widget.ListView;
import android.widget.ArrayAdapter;

public class PipelineStatusUpdateTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private ListView mSourceListView;
    private ListView mSinkListView;

    public PipelineStatusUpdateTask(VehicleManager vehicleService,
            Activity activity, ListView sourceListView, ListView sinkListView) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mSourceListView = sourceListView;
        mSinkListView = sinkListView;
    }

    public void run() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mSourceListView.setAdapter(new ArrayAdapter(mActivity,
                            android.R.layout.simple_list_item_1,
                            mVehicleManager.getSourceSummaries().toArray()));
                mSinkListView.setAdapter(new ArrayAdapter(mActivity,
                            android.R.layout.simple_list_item_1,
                            mVehicleManager.getSinkSummaries().toArray()));
            }
        });
    }
}
