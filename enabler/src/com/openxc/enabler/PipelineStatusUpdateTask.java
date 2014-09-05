package com.openxc.enabler;

import java.util.TimerTask;

import android.app.Activity;
import android.view.View;

import com.openxc.VehicleManager;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.network.NetworkVehicleInterface;
import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.sources.VehicleDataSource;
import com.openxc.sources.trace.TraceVehicleDataSource;

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

    private void setIconVisibility(
            Class<? extends VehicleDataSource> vehicleInterface,
            final View icon, VehicleInterfaceDescriptor viDescriptor) {
        if(viDescriptor != null &&
                viDescriptor.getInterfaceClass() == vehicleInterface) {
            if(icon.getVisibility() != View.VISIBLE) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO if connected, use white icon else red. or change
                        // opacity.
                        icon.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else if(icon.getVisibility() != View.GONE) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    icon.setVisibility(View.GONE);
                }
            });
        }
    }

    public void run() {
        final VehicleInterfaceDescriptor viDescriptor =
                mVehicleManager.getActiveVehicleInterface();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(viDescriptor == null) {
                    mNoneConnView.setVisibility(View.VISIBLE);
                } else {
                    mNoneConnView.setVisibility(View.GONE);
                }
            }
        });

        setIconVisibility(BluetoothVehicleInterface.class,
                mBluetoothConnView, viDescriptor);
        setIconVisibility(NetworkVehicleInterface.class, mNetworkConnView,
                viDescriptor);
        setIconVisibility(UsbVehicleInterface.class, mUsbConnView,
                viDescriptor);

        // TODO since the trace is not an official veh interface it will
        // never be returned from getActiveVehicleInterface - we'll need
        // some other way of checking in the Enabler
        setIconVisibility(TraceVehicleDataSource.class, mFileConnView,
                viDescriptor);
    }
}
