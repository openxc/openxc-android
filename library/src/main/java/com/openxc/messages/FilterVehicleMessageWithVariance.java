package com.openxc.messages;

import android.util.Log;

public class FilterVehicleMessageWithVariance {
    private  static final String TAG = "FilterVehicleMessage";
    private static String mMessage;
    private static String mValue;
    private static double lastValue,lastValue1,lastValue2,lastValue3,lastValue4,lastValue5,lastValue6,lastValue7,lastValue8,lastValue9 ;
    private static double varianceSpeed = 12.0,varianceFuel = 3.0,varianceFuleConssumed = 5.0,varianceOdometer = 5.0,varianceStearing = 12.0,varianceTorque = 40.0,varianceEngineSpeed = 200.0,varianceAccelerator = 5.0,varianceLongitude = .3,varianceLattitude = 2.0;



    public static boolean checkMessage( String message) {
        String[] separated = message.split(",");
        String temp1 = separated [1];
        String temp2 = separated [2];
        String[] separated1 = temp1.split("=");
        String[] separated2 = temp2.split("=");
        mMessage = separated1 [1];
        mValue = separated2 [1];

        return  false;
    }


    public static boolean checkMessage( SimpleVehicleMessage message) {

       String name = message.getName();
       String value = message.getValue().toString();

        if(name.compareToIgnoreCase("vehicle_speed") ==  0 ){
            //int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue + varianceSpeed || currentValue <= lastValue - varianceSpeed) {
             lastValue = currentValue;
                return  true;
            }
            lastValue = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("fuel_level") == 0){
            //int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue1 + varianceFuel || currentValue <= lastValue1 - varianceFuel) {
                lastValue1 = currentValue;
                return  true;
            }
            lastValue1 = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("fuel_consumed_since_restart") == 0 ){
            //int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue2 + varianceFuleConssumed || currentValue <= lastValue2 - varianceFuleConssumed) {
                lastValue2 = currentValue;
                return  true;
            }
            lastValue2 = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("odometer") == 0){
            //int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue3 + varianceOdometer || currentValue <= lastValue3 - varianceOdometer) {
                lastValue3 = currentValue;
                return  true;
            }
            lastValue3 = currentValue;
            return false;

        }
        else if(name.compareToIgnoreCase("steering_wheel_angle") == 0){
            //int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue4 + varianceStearing || currentValue <= lastValue4 - varianceStearing) {
                lastValue4 = currentValue;
                return  true;
            }
            lastValue4 = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("torque_at_transmission") == 0){
            //int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue5 + varianceTorque || currentValue <= lastValue5 - varianceTorque) {
                lastValue5 = currentValue;
                return  true;
            }
            lastValue5 = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("engine_speed") == 0 ){
           // int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue6 + varianceEngineSpeed || currentValue <= lastValue6 - varianceEngineSpeed) {
                lastValue6 = currentValue;
                return  true;
            }
            lastValue6 = currentValue;
            return  false;

        }
        else if( name.compareToIgnoreCase("accelerator_pedal_position") == 0){
            //int currentValue = Integer.parseInt(value);
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue7 + varianceAccelerator || currentValue <= lastValue7 - varianceAccelerator) {
                lastValue7 = currentValue;
                return  true;
            }
            lastValue7 = currentValue;
            return  false;


        }
        else if( name.compareToIgnoreCase("longitude") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue8 + varianceLongitude || currentValue <= lastValue8 - varianceLongitude) {
                lastValue8 = currentValue;
                return  true;
            }
            lastValue8 = currentValue;
            return  false;


        }else if( name.compareToIgnoreCase("latitude") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValue9 + varianceLattitude || currentValue <= lastValue9 - varianceLattitude) {
                lastValue9 = currentValue;
                return  true;
            }
            lastValue9 = currentValue;
            return  false;


        }else {
            Log.e(TAG, "Check Name Not Found: " +name );
        }

        return  true;

    }

}

