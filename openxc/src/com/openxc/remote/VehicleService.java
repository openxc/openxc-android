package com.openxc.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.openxc.DataPipeline;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.interfaces.VehicleInterfaceException;
import com.openxc.interfaces.VehicleInterfaceFactory;
import com.openxc.interfaces.VehicleInterfaceManagerUtils;
import com.openxc.interfaces.usb.UsbVehicleInterface;
import com.openxc.sinks.RemoteCallbackSink;
import com.openxc.sinks.VehicleDataSink;
import com.openxc.sources.ApplicationSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.VehicleDataSource;

/**
 * The VehicleService is the centralized source of all vehicle data.
 *
 * This server is intended to be a singleton on an Android device. All OpenXC
 * applciations funnel data to and from this service so they can share sources,
 * sinks and vehicle interfaces.
 *
 * Applications should not use this service directly, but should bind to the
 * in-process {@link com.openxc.VehicleManager} instead - that has an interface
 * that respects Measurement types. The interface used for the
 * VehicleService is purposefully primative as there are a small set of
 * objects that can be natively marshalled through an AIDL interface.
 *
 * By default, if the Android device supports uSB, the
 * {@link UsbVehicleInterface} is activated as a {@link VehicleInterface}. Other
 * vehicle interfaces can be activated with the
 * {@link #addVehicleInterface(Class, String)} method and they can removed with
 * {@link #removeVehicleInterface(Class)}.
 *
 * This service uses the same {@link com.openxc.DataPipeline} as the
 * {@link com.openxc.VehicleManager} to move data from sources to sinks, but it
 * the pipeline is not modifiable by the application as there is no good way to
 * pass running sources through the AIDL interface. The same style is used here
 * for clarity and in order to share code.
 */
public class VehicleService extends Service {
    private final static String TAG = "VehicleService";

    private DataPipeline mPipeline = new DataPipeline();
    private ApplicationSource mApplicationSource = new ApplicationSource();
    private CopyOnWriteArrayList<VehicleInterface> mInterfaces =
            new CopyOnWriteArrayList<VehicleInterface>();
    private RemoteCallbackSink mNotifier = new RemoteCallbackSink();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
    }

    /**
     * Shut down any associated services when this service is about to die.
     *
     * This stops the data source (e.g. stops trace playback) and kills the
     * thread used for notifying measurement listeners.
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "Service being destroyed");
        mPipeline.stop();
    }

    /**
     * Initialize the service and data source when a client binds to us.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);

        initializeDefaultSources();
        initializeDefaultSinks(mPipeline);
        return mBinder;
    }

    private void initializeDefaultSinks(DataPipeline pipeline) {
        pipeline.addSink(mNotifier);
    }

    private void initializeDefaultSources() {
        mPipeline.addSource(mApplicationSource);
        if(android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.HONEYCOMB) {
            addVehicleInterface(UsbVehicleInterface.class);
        }
    }

    private final VehicleServiceInterface.Stub mBinder =
        new VehicleServiceInterface.Stub() {
            public RawMeasurement get(String measurementId) {
                return mPipeline.get(measurementId);
            }

            public boolean send(RawMeasurement command) {
                return VehicleInterfaceManagerUtils.send(mInterfaces, command);
            }

            public void receive(RawMeasurement measurement) {
                mApplicationSource.handleMessage(measurement);
            }

            public void register(VehicleServiceListener listener) {
                Log.i(TAG, "Adding listener " + listener);
                mNotifier.register(listener);
            }

            public void unregister(VehicleServiceListener listener) {
                Log.i(TAG, "Removing listener " + listener);
                mNotifier.unregister(listener);
            }

            public int getMessageCount() {
                return VehicleService.this.mPipeline.getMessageCount();
            }

            public void addVehicleInterface(String interfaceName,
                    String resource) {
                VehicleService.this.addVehicleInterface(
                        interfaceName, resource);
            }

            public void removeVehicleInterface(String interfaceName) {
                VehicleService.this.removeVehicleInterface(interfaceName);
            }

            public List<String> getSourceSummaries() {
                ArrayList<String> sources = new ArrayList<String>();
                for(VehicleDataSource source : mPipeline.getSources()) {
                    sources.add(source.toString());
                }
                return sources;
            }

            public List<String> getSinkSummaries() {
                ArrayList<String> sinks = new ArrayList<String>();
                for(VehicleDataSink sink : mPipeline.getSinks()) {
                    sinks.add(sink.toString());
                }
                return sinks;
            }
    };

    private void addVehicleInterface(
            Class<? extends VehicleInterface> interfaceType) {
        addVehicleInterface(interfaceType, null);
    }

    private void addVehicleInterface(
            Class<? extends VehicleInterface> interfaceType,
            String resource) {
        VehicleInterface vehicleInterface =
            findActiveVehicleInterface(interfaceType);

        if(vehicleInterface == null) {
            try {
                vehicleInterface = VehicleInterfaceFactory.build(
                        interfaceType, VehicleService.this, resource);
            } catch(VehicleInterfaceException e) {
                Log.w(TAG, "Unable to add vehicle interface", e);
                return;
            }

            mInterfaces.add(vehicleInterface);
            mPipeline.addSource(vehicleInterface);
        } else {
            try {
                if(vehicleInterface.setResource(resource)) {
                    Log.d(TAG, "Changed resource of already active interface " +
                            vehicleInterface);
                } else {
                    Log.d(TAG, "Interface " + vehicleInterface +
                            " already had same active resource " + resource +
                            " -- not restarting");
                }
            } catch(DataSourceException e) {
                Log.w(TAG, "Unable to change resource", e);
            }
        }
        Log.i(TAG, "Added vehicle interface  " + vehicleInterface);
    }

    private void addVehicleInterface(String interfaceName, String resource) {
        try {
            addVehicleInterface(
                    VehicleInterfaceFactory.findClass(interfaceName), resource);
        } catch(VehicleInterfaceException e) {
            Log.w(TAG, "Unable to add vehicle interface", e);
        }
    }

    private void removeVehicleInterface(String interfaceName) {
        removeVehicleInterface(findActiveVehicleInterface(interfaceName));
    }

    private void removeVehicleInterface(VehicleInterface vehicleInterface) {
        if(vehicleInterface != null) {
            vehicleInterface.stop();
            mInterfaces.remove(vehicleInterface);
            mPipeline.removeSource(vehicleInterface);
        }
    }

    private VehicleInterface findActiveVehicleInterface(
            Class<? extends VehicleInterface> interfaceType) {
        for(VehicleInterface vehicleInterface : mInterfaces) {
            if(vehicleInterface.getClass().equals(interfaceType)) {
                return vehicleInterface;
            }
        }
        return null;
    }

    private VehicleInterface findActiveVehicleInterface(String interfaceName) {
        try {
            return findActiveVehicleInterface(
                    VehicleInterfaceFactory.findClass(interfaceName));
        } catch(VehicleInterfaceException e) {
            return null;
        }
    }
}
