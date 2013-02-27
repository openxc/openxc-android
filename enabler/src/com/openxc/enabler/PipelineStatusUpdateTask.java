package com.openxc.enabler;

import java.util.TimerTask;

import android.app.Activity;

import com.openxc.VehicleManager;
import android.widget.ListView;
import android.widget.ArrayAdapter;

public class PipelineStatusUpdateTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private ListView mSourceListView;
    private ListView mSinkListView;
    private ArrayAdapter<Object> mSourceListAdapter;
    private ArrayAdapter<Object> mSinkListAdapter;

    public PipelineStatusUpdateTask(VehicleManager vehicleService,
            Activity activity, ListView sourceListView, ListView sinkListView) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mSourceListView = sourceListView;
        mSinkListView = sinkListView;

        mSourceListAdapter = new ArrayAdapter<Object>(mActivity,
                android.R.layout.simple_list_item_1);
        mSourceListView.setAdapter(mSourceListAdapter);

        mSinkListAdapter = new ArrayAdapter<Object>(mActivity,
                android.R.layout.simple_list_item_1);
        mSinkListView.setAdapter(mSinkListAdapter);
    }

    public void run() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mSourceListAdapter.clear();
                mSourceListAdapter.addAll(
                    mVehicleManager.getSourceSummaries().toArray());
                mSourceListAdapter.notifyDataSetChanged();

                mSinkListAdapter.clear();
                mSinkListAdapter.addAll(
                    mVehicleManager.getSinkSummaries().toArray());
                mSinkListAdapter.notifyDataSetChanged();
            }
        });
    }
}
