package com.openxc.interfaces.usb;

import java.net.URI;

import android.content.Context;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

public class UsbModemVehicleInterface extends UsbVehicleInterface {
	private static final String TAG = "UsbModemVehicleInterface";

	public UsbModemVehicleInterface(SourceCallback callback, Context context,
			URI deviceUri) throws DataSourceException {
		super(callback, context, deviceUri);
		// TODO Auto-generated constructor stub
	}

	public UsbModemVehicleInterface(SourceCallback callback, Context context)
			throws DataSourceException {
		super(callback, context);
		// TODO Auto-generated constructor stub
	}

	public UsbModemVehicleInterface(Context context) throws DataSourceException {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public UsbModemVehicleInterface(Context context, String uriString)
			throws DataSourceException {
		super(context, uriString);
		// TODO Auto-generated constructor stub
	}

}
