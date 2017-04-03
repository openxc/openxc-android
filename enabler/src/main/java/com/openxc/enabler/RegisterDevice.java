package com.openxc.enabler;
/*Register Device is the device identifier
 * 
 * */
public final class RegisterDevice {
	static String deviceName = null;
	public static void setDevice(String val) {
		  deviceName = val;
    }
	 public static String getDevice() {
		  return deviceName;
   }
}
