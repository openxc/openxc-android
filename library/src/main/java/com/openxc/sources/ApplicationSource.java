package com.openxc.sources;

import com.google.common.base.MoreObjects;
import com.openxc.messages.VehicleMessage;

/**
 * A "reverse" source to pass externally generated measurements to the callback.
 *
 * This class is used by the {@link com.openxc.remote.VehicleService} to
 * pass measurements received from applications (e.g. from a trace file source
 * in an app) into the normal measurement workflow.
 */
public class ApplicationSource extends BaseVehicleDataSource {
    /**
     * Pass a raw measurement received from an external caller to the callback.
     *
     * Note that this method is public - users of this class can directly force
     * it to send new values.
     */
    @Override
    public void handleMessage(VehicleMessage measurement) {
        super.handleMessage(measurement);
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
