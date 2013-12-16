package com.openxc.enabler;

import java.util.List;
import java.util.TimerTask;

import android.app.Activity;

import com.openxc.VehicleManager;
import com.openxc.interfaces.InterfaceType;

import android.view.View;

public class PipelineStatusUpdateTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private View mUnknownConnView;
    private View mNetworkConnView;
    private View mBluetoothConnView;
    private View mUsbConnView;
    private View mFileConnView;
    private View mNoneConnView;

    public PipelineStatusUpdateTask(VehicleManager vehicleService,
            Activity activity, View unknownConnIV, View fileConnIV,
            View networkConnIV, View bluetoothConnIV, View usbConnIV,
            View noneConnView) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mUnknownConnView = unknownConnIV;
        mFileConnView = fileConnIV;
        mNetworkConnView = networkConnIV;
        mBluetoothConnView = bluetoothConnIV;
        mUsbConnView = usbConnIV;
        mNoneConnView = noneConnView;
    }

    private void setIconVisibility(InterfaceType interfaceType, final View icon,
            List<InterfaceType> activeInterfaceTypes){
        // If active and not visible => make visible
        if(activeInterfaceTypes.contains(interfaceType)
                && icon.getVisibility() != View.VISIBLE){
            mActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    icon.setVisibility(View.VISIBLE);
                }
            });
        }

        // If not active and not gone => make gone
        if(!activeInterfaceTypes.contains(interfaceType)
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
        List<InterfaceType> activeInterfaceTypes =
            mVehicleManager.getActiveSourceTypes();

        if(activeInterfaceTypes.isEmpty()){
            mActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    mNoneConnView.setVisibility(View.VISIBLE);

                    mBluetoothConnView.setVisibility(View.GONE);
                    mFileConnView.setVisibility(View.GONE);
                    mNetworkConnView.setVisibility(View.GONE);
                    mUsbConnView.setVisibility(View.GONE);
                    mUnknownConnView.setVisibility(View.GONE);
                }
            });
        } else {
            mActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    mNoneConnView.setVisibility(View.GONE);
                }
            });

            setIconVisibility(InterfaceType.BLUETOOTH, mBluetoothConnView,
                    activeInterfaceTypes);
            setIconVisibility(InterfaceType.FILE, mFileConnView,
                    activeInterfaceTypes);
            setIconVisibility(InterfaceType.NETWORK, mNetworkConnView,
                    activeInterfaceTypes);
            setIconVisibility(InterfaceType.USB, mUsbConnView,
                    activeInterfaceTypes);
            setIconVisibility(InterfaceType.UNKNOWN, mUnknownConnView,
                    activeInterfaceTypes);
        }
    }
}
