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
        Class<? extends VehicleInterface> interfaceType;
        try {
            interfaceType = Class.forName(interfaceName).asSubclass(
                    VehicleInterface.class);
        } catch(ClassNotFoundException e) {
            throw new VehicleInterfaceException(
                    "Couldn't find vehicle interface type " + interfaceName, e);
        }
        return interfaceType;
    }

    /**
     * Retrieve the Class for a given name and construct an instance of it.
     *
     * @throws VehicleInterfaceException If the named interfaced could not be
     *      found or if its constructor threw an exception.
     * @see #build(Class, Context, String)
     */
    public static VehicleInterface build(String interfaceName,
            Context context, String resource) throws VehicleInterfaceException {
        return build(findClass(interfaceName), context, resource);
    }

    /**
     * Retrieve the Class for a given name and construct an instance of it.
     *
     * @param interfaceType the desired class to load and instantiate.
     * @param context The Android application or service context to be passed to
     *      the new instance of the VehicleInterface.
     * @param resource A reference to a resource the new instance should use -
     *      see the specific implementation of {@link VehicleInterface} for its
     *      requirements.
     * @return A new instance of the class given in interfaceType.
     * @throws VehicleInterfaceException If the class' constructor threw an
     *      exception.
     */
    public static VehicleInterface build(
            Class<? extends VehicleInterface> interfaceType,
            Context context, String resource) throws VehicleInterfaceException {
        Log.d(TAG, "Constructing new instance of " + interfaceType
                + " with resource " + resource);
        Constructor<? extends VehicleInterface> constructor;
        try {
            constructor = interfaceType.getConstructor(
                    Context.class, String.class);
        } catch(NoSuchMethodException e) {
            throw new VehicleInterfaceException(interfaceType +
                    " doesn't have a proper constructor", e);
        }

        String message;
        Exception error;
        try {
            return constructor.newInstance(context, resource);
        } catch(InstantiationException e) {
            message = "Couldn't instantiate vehicle interface " +
                interfaceType;
            error = e;
        } catch(IllegalAccessException e) {
            message = "Default constructor is not accessible on " +
                interfaceType;
            error = e;
        } catch(InvocationTargetException e) {
            message = interfaceType + "'s constructor threw an exception";
            error = e;
        }
        throw new VehicleInterfaceException(message, error);
    }
}
