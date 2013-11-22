package com.openxc.interfaces;

import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.interfaces.network.NetworkVehicleInterface;
import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.sources.VehicleDataSource;
import com.openxc.sources.trace.TraceVehicleDataSource;

public enum InterfaceType {
    BLUETOOTH("BLUETOOTH"),
    FILE("FILE"),
    NETWORK("NETWORK"),
    USB("USB"),
    UNKNOWN("UNKNOWN");

    private String stringValue;
    private InterfaceType(String toString){
        stringValue = toString;
    }

    public static InterfaceType interfaceTypeFromClass(
            VehicleDataSource source){

        if(source == null){
            return UNKNOWN;
        } else if(source instanceof BluetoothVehicleInterface){
            return BLUETOOTH;
        } else if(source instanceof TraceVehicleDataSource){
            return FILE;
        } else if(source instanceof NetworkVehicleInterface){
            return NETWORK;
        } else if(source instanceof UsbVehicleInterface){
            return USB;
        } else {
            return UNKNOWN;
        }
    }

    public static InterfaceType interfaceTypeFromString(
            String vInterfaceString){

        if(vInterfaceString == null){
            return UNKNOWN;
        } else if(vInterfaceString.equalsIgnoreCase("BLUETOOTH")){
            return BLUETOOTH;
        } else if(vInterfaceString.equalsIgnoreCase("FILE")){
            return FILE;
        } else if(vInterfaceString.equalsIgnoreCase("NETWORK")){
            return NETWORK;
        } else if(vInterfaceString.equalsIgnoreCase("USB")){
            return USB;
        } else {
            return UNKNOWN;
        }
    }

    @Override
    public String toString(){
        return stringValue;
    }
}
