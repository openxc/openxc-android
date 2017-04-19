package com.openxc.enabler;

import java.util.TimerTask;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import com.openxc.VehicleManager;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.bluetooth.BluetoothModemVehicleInterface;
import com.openxc.interfaces.bluetooth.BluetoothV2XVehicleInterface;
import com.openxc.interfaces.network.NetworkVehicleInterface;
import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.interfaces.usb.UsbModemVehicleInterface;
import com.openxc.sources.VehicleDataSource;
import com.openxcplatform.enabler.R;

public class PipelineStatusUpdateTask extends TimerTask {
    private VehicleManager mVehicleManager;
    private Activity mActivity;
    private View mNetworkConnView;
    private View mBluetoothConnView;
    private View mBluetoothModemConnView;
    private View mBluetoothV2XConnView;
    private View mUsbConnView;
    private View mUsbModemConnView;
    private View mFileConnView;
    private View mNoneConnView;
    private TextView mModemViDeviceIdView;
    private TextView mModemViV2XDeviceIdView;

    public PipelineStatusUpdateTask(VehicleManager vehicleService,
            Activity activity, View fileConnView,
            View networkConnView, View bluetoothConnView, View bluetoothModemConnView,
	    View usbConnView, View usbModemConnView,
            View noneConnView, TextView modemViDeviceIdView,
	    View bluetoothV2XConnView, TextView modemViV2XDeviceIdView) {
        mVehicleManager = vehicleService;
        mActivity = activity;
        mFileConnView = fileConnView;
        mNetworkConnView = networkConnView;
        mBluetoothConnView = bluetoothConnView;
	    mBluetoothModemConnView = bluetoothModemConnView;
	    mBluetoothV2XConnView = bluetoothV2XConnView;
        mUsbConnView = usbConnView;
        mUsbModemConnView = usbModemConnView;
	    mNoneConnView = noneConnView;
	    mModemViDeviceIdView = modemViDeviceIdView;
	    mModemViV2XDeviceIdView = modemViV2XDeviceIdView;
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

			if ((viDescriptor != null ) && (viDescriptor.getInterfaceClass() == BluetoothVehicleInterface.class))
        	{
	        	boolean isModem = mModemViDeviceIdView.getText().toString().startsWith(BluetoothModemVehicleInterface.DEVICE_NAME_PREFIX);
	        	boolean isV2X = mModemViV2XDeviceIdView.getText().toString().startsWith(BluetoothV2XVehicleInterface.DEVICE_NAME_PREFIX);
        		setVisibility(mBluetoothConnView, !isModem);
        		setVisibility(mBluetoothModemConnView, isModem);
        		if(!isModem && isV2X)
        			setVisibility(mBluetoothV2XConnView, isV2X);
        	}
        	else
        	{
	        	setVisibility(mBluetoothConnView, true);
        		setVisibility(mBluetoothModemConnView, true);
        		setVisibility(mBluetoothV2XConnView, true);
        	}
			
        	setVisibility(NetworkVehicleInterface.class, mNetworkConnView,
                	viDescriptor);
        	setVisibility(UsbVehicleInterface.class, mUsbConnView,
                	viDescriptor);
        	setVisibility(UsbModemVehicleInterface.class, mUsbModemConnView,
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
