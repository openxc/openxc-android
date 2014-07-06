package com.openxc.enabler;

import java.util.List;
import java.util.TimerTask;

import android.app.Activity;

import com.openxc.VehicleManager;
import com.openxc.sources.VehicleDataSource;
import com.openxc.sources.trace.TraceVehicleDataSource;
import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.network.NetworkVehicleInterface;

import android.view.View;

public class PipelineStatusUpdateTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private View mNetworkConnView;
    private View mBluetoothConnView;
    private View mUsbConnView;
    private View mFileConnView;
    private View mNoneConnView;

    public PipelineStatusUpdateTask(VehicleManager vehicleService,
            Activity activity, View fileConnView,
            View networkConnView, View bluetoothConnView, View usbConnView,
            View noneConnView) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mFileConnView = fileConnView;
        mNetworkConnView = networkConnView;
        mBluetoothConnView = bluetoothConnView;
        mUsbConnView = usbConnView;
        mNoneConnView = noneConnView;
    }

    private void setIconVisibility(Class<? extends VehicleDataSource> source,
            final View icon,
            List<Class<? extends VehicleDataSource>> activeSources){
        // If active and not visible => make visible
        if(activeSources.contains(source)
                && icon.getVisibility() != View.VISIBLE){
            mActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    icon.setVisibility(View.VISIBLE);
                }
            });
        }

        // If not active and not gone => make gone
        if(!activeSources.contains(source)
                && (icon.getVisibility() != View.GONE)){
            mActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    icon.setVisibility(View.GONE);
                }
            });
        }
    }

    public void run() {
        List<Class<? extends VehicleDataSource>> activeSources =
            mVehicleManager.getActiveSources();

        if(activeSources.isEmpty()){
            mActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    mNoneConnView.setVisibility(View.VISIBLE);

                    mBluetoothConnView.setVisibility(View.GONE);
                    mFileConnView.setVisibility(View.GONE);
                    mNetworkConnView.setVisibility(View.GONE);
                    mUsbConnView.setVisibility(View.GONE);
                }
            });
        } else {
            mActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    mNoneConnView.setVisibility(View.GONE);
                }
            });

            setIconVisibility(BluetoothVehicleInterface.class,
                    mBluetoothConnView, activeSources);
            setIconVisibility(TraceVehicleDataSource.class, mFileConnView,
                    activeSources);
            setIconVisibility(NetworkVehicleInterface.class, mNetworkConnView,
                    activeSources);
            setIconVisibility(UsbVehicleInterface.class, mUsbConnView,
                    activeSources);
        }
    }
}
