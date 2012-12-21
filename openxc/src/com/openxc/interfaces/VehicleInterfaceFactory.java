package com.openxc.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import android.content.Context;
import android.util.Log;

public class VehicleInterfaceFactory {
    private static final String TAG = "VehicleInterfaceFactory";

    public static VehicleInterface build(Context context, String interfaceName,
            String resource) throws VehicleInterfaceException {
        Log.d(TAG, "Looking up class for name " + interfaceName);
        Class<? extends VehicleInterface> interfaceType;
        try {
            interfaceType = Class.forName(interfaceName).asSubclass(
                    VehicleInterface.class);
            Log.d(TAG, "Found " + interfaceType);
        } catch(ClassNotFoundException e) {
            String message = "Couldn't find vehicle interface type " +
                    interfaceName;
            Log.w(TAG, message, e);
            throw new VehicleInterfaceException(message, e);
        }

        return build(context, interfaceType, resource);
    }

    public static VehicleInterface build(Context context,
            Class<? extends VehicleInterface> interfaceType,
            String resource) throws VehicleInterfaceException {
        Log.d(TAG, "Constructing new instance of " + interfaceType
                + " with resource " + resource);
        Constructor<? extends VehicleInterface> constructor;
        try {
            constructor = interfaceType.getConstructor(
                    Context.class, String.class);
        } catch(NoSuchMethodException e) {
            String message = interfaceType +
                    " doesn't have a proper constructor";
            Log.w(TAG, message);
            throw new VehicleInterfaceException(message, e);
        }

        try {
            return constructor.newInstance(context, resource);
        } catch(InstantiationException e) {
            String error = "Couldn't instantiate vehicle interface " + interfaceType;
            Log.w(TAG, error, e);
            throw new VehicleInterfaceException(error);
        } catch(IllegalAccessException e) {
            String error = "Default constructor is not accessible on " + interfaceType;
            Log.w(TAG, error, e);
            throw new VehicleInterfaceException(error);
        } catch(InvocationTargetException e) {
            String error = interfaceType + "'s constructor threw an exception";
            Log.w(TAG, error, e);
            throw new VehicleInterfaceException(error);
        }
    }
}
