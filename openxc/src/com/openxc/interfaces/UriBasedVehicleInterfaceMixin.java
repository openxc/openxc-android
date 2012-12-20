package com.openxc.interfaces;

import java.net.URI;
import java.net.URISyntaxException;

import android.util.Log;

import com.openxc.sources.DataSourceException;

public class UriBasedVehicleInterfaceMixin {
    private final static String TAG = "UriBasedVehicleInterfaceMixin";

    /**
     * Return true if the given address and port match those currently in use by
     * the network data source.
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
     * Return true if the address and port are valid.
     *
     * @return true if the address and port are valid.
     */
    public static boolean validateResource(String uriString) {
        if(uriString == null) {
            return false;
        }

        try {
            createUri(uriString);
            return true;
        } catch(DataSourceException e) {
            Log.d(TAG, "URI is not valid", e);
            return false;
        }
    }

    public static URI createUri(String uriString) throws DataSourceException {
        try {
            return new URI(uriString);
        } catch(URISyntaxException e) {
            throw new DataSourceException("Not a valid URI: " + uriString, e);
        }
    }

}
