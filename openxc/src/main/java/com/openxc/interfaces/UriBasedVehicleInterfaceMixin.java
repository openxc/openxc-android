package com.openxc.interfaces;

import java.net.URI;
import java.net.URISyntaxException;

import android.util.Log;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

/**
 * A collection of utilites for vehicle interfaces that reference a physical
 * device with a URI.
 */
public class UriBasedVehicleInterfaceMixin {
    private final static String TAG = "UriBasedVehicleInterfaceMixin";

    /**
     * Determine if two URIs refer to the same resource.
     *
     * The function safely attempts to convert the otherResource parameter to a
     * URI object before comparing.
     *
     * @return true if the address and port match the current in-use values.
     */
    public static boolean sameResource(URI uri, String otherResource) {
        try {
            return createUri(otherResource).equals(uri);
        } catch(DataSourceException e) {
            return false;
        }
    }

    /**
     * Convert the parameter to a URI and validate the correctness of its host
     * and port.
     *
     * @return true if the address and port are valid.
     */
    public static boolean validateResource(String uriString) {
        if(uriString == null) {
            return false;
        }

        try {
            return validateResource(createUri(uriString));
        } catch(DataSourceException e) {
            Log.d(TAG, "URI is not valid", e);
            return false;
        }
    }

    /**
     * Validate the correctness of the host and port in a given URI.
     *
     * @return true if the address and port are valid.
     */
    public static boolean validateResource(URI uri) {
        return uri != null && uri.getPort() != -1 && uri.getHost() != null;
    }

    /**
     * Attempt to construct an instance of URI from the given String.
     *
     * @param uriString the String representation of the possible URI.
     * @throws DataSourceException if the parameter is not a valid URI.
     */
    public static URI createUri(String uriString) throws DataSourceException {
        if(uriString == null) {
            throw new DataSourceResourceException("URI string is null");
        }

        try {
            return new URI(uriString);
        } catch(URISyntaxException e) {
            throw new DataSourceResourceException("Not a valid URI: " +
                    uriString, e);
        }
    }
}
