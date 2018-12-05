package com.openxc.interfaces.bluetooth;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

import android.content.Context;

public class BluetoothV2XVehicleInterface extends BluetoothVehicleInterface{
	
	public static final String DEVICE_NAME_PREFIX = BluetoothVehicleInterface.DEVICE_NAME_PREFIX + "V2X-";
	
	public BluetoothV2XVehicleInterface(SourceCallback callback,
			Context context, String address) throws DataSourceException {
		super(callback, context, address);
		
	}

	public BluetoothV2XVehicleInterface(Context context, String address)
			throws DataSourceException {
		super(context, address);
		
	}


}
