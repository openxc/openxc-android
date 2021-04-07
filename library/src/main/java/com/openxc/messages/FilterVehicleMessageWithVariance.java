package com.openxc.messages;

import android.util.Log;

public class FilterVehicleMessageWithVariance {
    private  static final String TAG = "FilterVehicleMessage";
    private static String mMessage;
    private static String mValue;
    private static double lastValue,lastValue1,lastValue2,lastValue3,lastValue4,lastValue5,lastValue6,lastValue7 ;
    private static double variance = 1.0;


    public static boolean checkMessage(String name,Object value) {
        Log.e(TAG, "checkNameValue: "  + name + value.toString());

        return  false;
    }
    public static boolean checkMessage( String message) {
        String[] separated = message.split(",");
        String temp1 = separated [1];
        String temp2 = separated [2];
        String[] separated1 = temp1.split("=");
        String[] separated2 = temp2.split("=");
        mMessage = separated1 [1];
        mValue = separated2 [1];
        Log.e(TAG, "checkMessage1: " + mMessage);
        Log.e(TAG, "checkMessage2: " + mValue );


        return  false;
    }


    public static boolean checkMessage( SimpleVehicleMessage message) {

       String name = message.getName();
       String value = message.getValue().toString();
        Log.e(TAG, "checkNameValue: "  + name   + "," + value);

        if(name.compareToIgnoreCase("vehicle_speed") ==  0 ){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue + variance || currentValue <= lastValue - variance) {
             lastValue = currentValue;
                return  true;
            }
            lastValue = currentValue;
        
        }
        if( name.compareToIgnoreCase("fuel_level") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue1 + variance || currentValue <= lastValue1 - variance) {
                lastValue1 = currentValue;
                return  true;
            }
            lastValue1 = currentValue;

        }
        if( name.compareToIgnoreCase("fuel_consumed_since_restart") == 0 ){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue2 + variance || currentValue <= lastValue2 - variance) {
                lastValue2 = currentValue;
                return  true;
            }
            lastValue2 = currentValue;

        }
        if( name.compareToIgnoreCase("odometer") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue3 + variance || currentValue <= lastValue3 - variance) {
                lastValue3 = currentValue;
                return  true;
            }
            lastValue3 = currentValue;

        }
        if(name.compareToIgnoreCase("steering_wheel_angle") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue4 + variance || currentValue <= lastValue4 - variance) {
                lastValue4 = currentValue;
                return  true;
            }
            lastValue4 = currentValue;

        }
        if( name.compareToIgnoreCase("torque_at_transmission") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue5 + variance || currentValue <= lastValue5 - variance) {
                lastValue5 = currentValue;
                return  true;
            }
            lastValue5 = currentValue;

        }
        if( name.compareToIgnoreCase("engine_speed") == 0 ){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue6 + variance || currentValue <= lastValue6 - variance) {
                lastValue6 = currentValue;
                return  true;
            }
            lastValue6 = currentValue;

        }
        if( name.compareToIgnoreCase("accelerator_pedal_position") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue7 + variance || currentValue <= lastValue7 - variance) {
                lastValue7 = currentValue;
                return  true;
            }
            lastValue7 = currentValue;


        }

        return  false;
    }
//    if (lastspeed >= lastspeed + change value or lastspeed <= lastspeed - change value)
//    { Return listener}
}

