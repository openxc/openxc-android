package com.openxc.enabler;

import java.util.TimerTask;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;

import com.openxc.VehicleManager;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.network.NetworkVehicleInterface;
import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.sources.VehicleDataSource;
import com.openxcplatform.enabler.R;

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

    private void setVisibility(
            Class<? extends VehicleDataSource> vehicleInterface,
            final View view, VehicleInterfaceDescriptor viDescriptor) {
        setVisibility(view, viDescriptor != null &&
                viDescriptor.getInterfaceClass() == vehicleInterface);
    }

    private void setVisibility(final View view, boolean visible) {
        if(mActivity == null) {
            return;
        }

        if(visible && view.getVisibility() != View.VISIBLE) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.VISIBLE);
                }
            });
        } else if(!visible && view.getVisibility() != View.GONE) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                }
            });
        }
    }

    public void run() {
        if(mVehicleManager != null) {
            final VehicleInterfaceDescriptor viDescriptor =
                    mVehicleManager.getActiveVehicleInterface();

            setVisibility(BluetoothVehicleInterface.class,
                    mBluetoothConnView, viDescriptor);
            setVisibility(NetworkVehicleInterface.class, mNetworkConnView,
                    viDescriptor);
            setVisibility(UsbVehicleInterface.class, mUsbConnView,
                    viDescriptor);

            setVisibility(mNoneConnView,
                    !traceEnabled() && viDescriptor == null);
            setVisibility(mFileConnView, traceEnabled());
        }
    }

    private boolean traceEnabled() {
        return mActivity != null &&
            PreferenceManager.getDefaultSharedPreferences(mActivity).getString(
                mActivity.getString(R.string.vehicle_interface_key), "").equals(
                mActivity.getString(R.string.trace_interface_option_value));
    }
}
