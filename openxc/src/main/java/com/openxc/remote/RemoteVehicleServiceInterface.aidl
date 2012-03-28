package com.openxc.remote;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;
import com.openxc.remote.RawMeasurement;

/**
 * The AIDL interface for a RemoteVehicleService running in a separate process.
 *
 * This interface is used to bind to an AIDL interface in order to interact with
 * a centralized RemoteVehicleService that is reading data from a vehicle, trace
 * file or other source.
 *
 * Applications should use the in-process VehicleService, as that builds the
 * proper Measurement types before returning - the data at this level is very
 * loosely typed in order to slip through the limited AIDL interface.
 */
interface RemoteVehicleServiceInterface {
    RawMeasurement get(String measurementType);

    void addListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);
    void removeListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);

    void setDataSource(String dataSource, String resource);
    void enableRecording(boolean enabled);
    int getMessageCount();
}
