package com.openxc.messages;

import android.util.Log;

public class FilterVehicleMessageWithVariance {
    private  static final String TAG = "FilterVehicleMessage";
    private static String mMessage;
    private static String mValue;
    private static double lastValueSpeed,lastValueFuel,lastValueFuelConsumed,lastValueOdometer,lastValueStearing,lastValueTorque,lastValueEngineSpeed,lastValueAccelerator,lastValueLongitude,lastValueLattitude ;
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

            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueSpeed + varianceSpeed || currentValue <= lastValueSpeed - varianceSpeed) {
                lastValueSpeed = currentValue;
                return  true;
            }
            lastValueSpeed = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("fuel_level") == 0){

            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueFuel + varianceFuel || currentValue <= lastValueFuel - varianceFuel) {
                lastValueFuel = currentValue;
                return  true;
            }
            lastValueFuel = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("fuel_consumed_since_restart") == 0 ){

            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueFuelConsumed + varianceFuleConssumed || currentValue <= lastValueFuelConsumed - varianceFuleConssumed) {
                lastValueFuelConsumed = currentValue;
                return  true;
            }
            lastValueFuelConsumed = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("odometer") == 0){

            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueOdometer + varianceOdometer || currentValue <= lastValueOdometer - varianceOdometer) {
                lastValueOdometer = currentValue;
                return  true;
            }
            lastValueOdometer = currentValue;
            return false;

        }
        else if(name.compareToIgnoreCase("steering_wheel_angle") == 0){

            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueStearing + varianceStearing || currentValue <= lastValueStearing - varianceStearing) {
                lastValueStearing = currentValue;
                return  true;
            }
            lastValueStearing = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("torque_at_transmission") == 0){

            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueTorque + varianceTorque || currentValue <= lastValueTorque - varianceTorque) {
                lastValueTorque = currentValue;
                return  true;
            }
            lastValueTorque = currentValue;
            return false;

        }
        else if( name.compareToIgnoreCase("engine_speed") == 0 ){

            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueEngineSpeed + varianceEngineSpeed || currentValue <= lastValueEngineSpeed - varianceEngineSpeed) {
                lastValueEngineSpeed = currentValue;
                return  true;
            }
            lastValueEngineSpeed = currentValue;
            return  false;

        }
        else if( name.compareToIgnoreCase("accelerator_pedal_position") == 0){
           
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueAccelerator + varianceAccelerator || currentValue <= lastValueAccelerator - varianceAccelerator) {
                lastValueAccelerator = currentValue;
                return  true;
            }
            lastValueAccelerator = currentValue;
            return  false;


        }
        else if( name.compareToIgnoreCase("longitude") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueLongitude + varianceLongitude || currentValue <= lastValueLongitude - varianceLongitude) {
                lastValueLongitude = currentValue;
                return  true;
            }
            lastValueLongitude = currentValue;
            return  false;


        }else if( name.compareToIgnoreCase("latitude") == 0){
            double currentValue = Double.parseDouble(value);

            if(currentValue > lastValueLattitude + varianceLattitude || currentValue <= lastValueLattitude - varianceLattitude) {
                lastValueLattitude = currentValue;
                return  true;
            }
            lastValueLattitude = currentValue;
            return  false;


        }else {
            Log.e(TAG, "Check Name Not Found: " +name );
        }

        return  true;

    }

}

