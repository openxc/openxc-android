package com.openxc.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import android.content.Context;
import android.util.Log;

/**
 * A factory that uses reflection to create instance of VehicleInterface
 * implementations.
 */
public class VehicleInterfaceFactory {
    private static final String TAG = "VehicleInterfaceFactory";

    /**
     * Obtain the Class object for a given VehicleInterface class name.
     *
     * The class must be in the classpath of the process' context, or an
     * exception will be thrown.
     *
     * @param interfaceName the canonical name of class implementing
     *      {@link VehicleInterface}
     * @return the Class object, if found.
     * @throws VehicleInterfaceException if the named class could not be found
     *      or loaded.
     *
     */
    public static Class<? extends VehicleInterface> findClass(
            String interfaceName) throws VehicleInterfaceException {
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
        return interfaceType;
    }

    public static VehicleInterface build(Context context, String interfaceName,
            String resource) throws VehicleInterfaceException {
        return build(context, findClass(interfaceName), resource);
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
