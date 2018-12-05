package com.openxc.interfaces.bluetooth;

import android.content.Context;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

public class BluetoothModemVehicleInterface extends BluetoothVehicleInterface {

	private static final String TAG = "BluetoothModemVehicleInterface";
	public static final String DEVICE_NAME_PREFIX = BluetoothVehicleInterface.DEVICE_NAME_PREFIX + "MODEM-";
	
	public BluetoothModemVehicleInterface(SourceCallback callback,
                                          Context context, String address) throws DataSourceException {
		super(callback, context, address);
		// TODO Auto-generated constructor stub
	}

	public BluetoothModemVehicleInterface(Context context, String address)
			throws DataSourceException {
		super(context, address);
		// TODO Auto-generated constructor stub
	}
}
