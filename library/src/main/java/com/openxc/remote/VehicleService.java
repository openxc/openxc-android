package com.openxc.remote;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.openxc.DataPipeline;
import com.openxc.interfaces.VehicleInterface;
import com.openxc.interfaces.VehicleInterfaceDescriptor;
import com.openxc.interfaces.VehicleInterfaceException;
import com.openxc.interfaces.VehicleInterfaceFactory;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;
import com.openxc.messages.MessageKey;
import com.openxc.messages.VehicleMessage;
import com.openxc.sinks.DataSinkException;
import com.openxc.sinks.RemoteCallbackSink;
import com.openxc.sources.ApplicationSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.NativeLocationSource;
import com.openxc.sources.VehicleDataSource;
import com.openxc.sources.WakeLockManager;

import com.openxcplatform.R;

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
 * VehicleService is purposefully primitive as there are a small set of
 * objects that can be natively marshalled through an AIDL interface.
 *
 * Only one vehicle interface can be active at at time, and it can be set with
 * the {@link #setVehicleInterface(String, String)} method.
 *
 * This service uses the same {@link com.openxc.DataPipeline} as the
 * {@link com.openxc.VehicleManager} to move data from sources to sinks, but it
 * the pipeline is not modifiable by the application as there is no good way to
 * pass running sources through the AIDL interface. The same style is used here
 * for clarity and in order to share code.
 */
public class VehicleService extends Service implements DataPipeline.Operator {
    private final static String TAG = "VehicleService";

    private final static int SERVICE_NOTIFICATION_ID = 1000;

    // Work around an issue with instrumentation tests and foreground services
    // https://code.google.com/p/android/issues/detail?id=12122
    public static boolean sIsUnderTest = false;

    private boolean mForeground = false;
    private DataPipeline mPipeline = new DataPipeline(this);
    private ApplicationSource mApplicationSource = new ApplicationSource();
    private VehicleDataSource mNativeLocationSource;
    private VehicleInterface mVehicleInterface;
    private RemoteCallbackSink mNotifier = new RemoteCallbackSink();
    private WakeLockManager mWakeLocker;
    private boolean mUserPipelineActive;
    private final RemoteCallbackList<ViConnectionListener> mViConnectionListeners =
            new RemoteCallbackList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        mWakeLocker = new WakeLockManager(this, TAG);
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

    private void moveToForeground() {
        if(!mForeground) {
            Log.i(TAG, "Moving service to foreground.");

            try {
                // I'd like to not have to depend on the EnablerActivity, but
                // the notification needs to have some application it starts
                // when the users clicks the notification.
                Intent intent = new Intent(this,
                        Class.forName("com.openxc.enabler.OpenXcEnablerActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this, 0, intent, 0);

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this);
                notificationBuilder.setContentTitle(getString(R.string.openxc_name))
                        .setContentInfo(getString(R.string.notification_content))
                        .setSmallIcon(R.drawable.openxc_notification_icon_small_white)
                        .setContentIntent(pendingIntent);

                startForeground(SERVICE_NOTIFICATION_ID,
                        notificationBuilder.build());
            } catch (ClassNotFoundException e) {
                // This may happen if you are running the instrumentation tests
                // and it's not an error
            }
            mForeground = true;
        }
    }

    private void removeFromForeground() {
        if(mForeground) {
            Log.i(TAG, "Removing service from foreground.");

            if(!sIsUnderTest) {
                stopForeground(true);
            }
            mForeground = false;
        }
    }

    private void initializeDefaultSinks(DataPipeline pipeline) {
        pipeline.addSink(mNotifier);
    }

    private void initializeDefaultSources() {
        mPipeline.addSource(mApplicationSource);
    }

    private final VehicleServiceInterface.Stub mBinder =
        new VehicleServiceInterface.Stub() {
            @Override
            public VehicleMessage get(MessageKey key) {
                return mPipeline.get(key);
            }

            @Override
            public boolean send(VehicleMessage command) {
                command.untimestamp();
                boolean sent = false;
                synchronized(VehicleService.this) {
                    if(mVehicleInterface != null && mVehicleInterface.isConnected()) {
                        try {
                            mVehicleInterface.receive(command);
                            Log.d(TAG, "Sent " + command + " using interface " +
                                    mVehicleInterface);
                            sent = true;
                        } catch(DataSinkException e) {
                            Log.w(TAG, mVehicleInterface +
                                    " unable to send command", e);
                        }
                    } else {
                        Log.w(TAG, "No connected VI available to send command");
                    }
                }
                return sent;
            }

            @Override
            public void receive(VehicleMessage measurement) {
                mApplicationSource.handleMessage(measurement);
            }

            @Override
            public void register(VehicleServiceListener listener) {
                Log.i(TAG, "Adding listener " + listener);
                mNotifier.register(listener);
            }

            @Override
            public void unregister(VehicleServiceListener listener) {
                Log.i(TAG, "Removing listener " + listener);
                mNotifier.unregister(listener);
            }

            @Override
            public int getMessageCount() {
                return VehicleService.this.mPipeline.getMessageCount();
            }

            @Override
            public void setVehicleInterface(String interfaceName,
                    String resource) {
                VehicleService.this.setVehicleInterface(
                        interfaceName, resource);
            }

            @Override
            public void addViConnectionListener(ViConnectionListener listener) {
                VehicleService.this.addViConnectionListener(listener);
            }

            @Override
            public void setBluetoothPollingStatus(boolean enabled) {
                VehicleService.this.setBluetoothPollingStatus(enabled);
            }

            @Override
            public void setNativeGpsStatus(boolean enabled) {
                VehicleService.this.setNativeGpsStatus(enabled);
            }

            @Override
            public VehicleInterfaceDescriptor getVehicleInterfaceDescriptor() {
                VehicleInterfaceDescriptor descriptor = null;
                synchronized(VehicleService.this) {
                    if(mVehicleInterface != null) {
                        descriptor = new VehicleInterfaceDescriptor(mVehicleInterface);
                    }
                }
                return descriptor;
            }

            @Override
            public void userPipelineActivated() {
                mUserPipelineActive = true;
                VehicleService.this.onPipelineActivated();
            }

            @Override
            public void userPipelineDeactivated() {
                mUserPipelineActive = false;
                if(!VehicleService.this.mPipeline.isActive()) {
                    VehicleService.this.onPipelineDeactivated();
                }
            }

            @Override
            public boolean isViConnected() {
                return VehicleService.this.mPipeline.isActive();
            }
    };

    private void addViConnectionListener(ViConnectionListener listener) {
        synchronized(mViConnectionListeners) {
            mViConnectionListeners.register(listener);
        }
    }

    private void setVehicleInterface(String interfaceName, String resource) {
        Class<? extends VehicleInterface> interfaceType = null;

        if(interfaceName != null) {
            try {
                interfaceType = VehicleInterfaceFactory.findClass(interfaceName);
            } catch(VehicleInterfaceException e) {
                Log.w(TAG, "Unable to find VI matching " + interfaceName +
                        " -- disabling current interface");
            }
        }

        synchronized(this) {
            if(mVehicleInterface != null && (interfaceName == null ||
                    (interfaceType != null &&
                     !mVehicleInterface.getClass().isAssignableFrom(
                         interfaceType)))) {
                Log.i(TAG, "Disabling currently active VI " + mVehicleInterface);
                mVehicleInterface.stop();
                mPipeline.removeSource(mVehicleInterface);
                mVehicleInterface = null;
            }

            if(interfaceName != null && interfaceType != null) {
                if(mVehicleInterface == null ||
                        !mVehicleInterface.getClass().isAssignableFrom(
                            interfaceType)) {
                    try {
                        mVehicleInterface = VehicleInterfaceFactory.build(
                                interfaceType, VehicleService.this, resource);
                    } catch(VehicleInterfaceException e) {
                        Log.w(TAG, "Unable to set vehicle interface", e);
                        return;
                    }

                    mPipeline.addSource(mVehicleInterface);
                } else {
                    try {
                        if(mVehicleInterface.setResource(resource)) {
                            Log.d(TAG, "Changed resource of already " +
                                    "active interface " + mVehicleInterface);
                        } else {
                            Log.d(TAG, "Interface " + mVehicleInterface +
                                    " already had same active resource " + resource +
                                    " -- not restarting");
                        }
                    } catch(DataSourceException e) {
                        Log.w(TAG, "Unable to change resource", e);
                    }
                }
            }
            Log.i(TAG, "Set vehicle interface to " + mVehicleInterface);
        }
    }

    private void setNativeGpsStatus(boolean enabled) {
        Log.i(TAG, "Setting native GPS to " + enabled);
        if(enabled && mNativeLocationSource == null) {
            mNativeLocationSource = new NativeLocationSource(this);
            mPipeline.addSource(mNativeLocationSource);
        } else if(!enabled) {
            mPipeline.removeSource(mNativeLocationSource);
            mNativeLocationSource = null;
        }
    }

    private synchronized void setBluetoothPollingStatus(boolean enabled) {
        if(mVehicleInterface != null &&
                mVehicleInterface instanceof BluetoothVehicleInterface) {
            ((BluetoothVehicleInterface)mVehicleInterface).setPollingStatus(
                    enabled);
        }
    }

    @Override
    public synchronized void onPipelineActivated() {
        mWakeLocker.acquireWakeLock();
        moveToForeground();
        if(mVehicleInterface != null && mVehicleInterface.isConnected()) {
            VehicleInterfaceDescriptor descriptor =
                        new VehicleInterfaceDescriptor(mVehicleInterface);
            synchronized(mViConnectionListeners) {
                int i = mViConnectionListeners.beginBroadcast();
                while(i > 0) {
                    i--;
                    try {
                        mViConnectionListeners.getBroadcastItem(i).onConnected(descriptor);
                    } catch(RemoteException e) {
                        Log.w(TAG, "Couldn't notify VI connection " +
                                "listener -- did it crash?", e);
                    }
                }
                mViConnectionListeners.finishBroadcast();
            }
        }
    }

    @Override
    public void onPipelineDeactivated() {
        if(!mUserPipelineActive) {
            mWakeLocker.releaseWakeLock();
            removeFromForeground();

            synchronized(this) {
                if(mVehicleInterface == null || !mVehicleInterface.isConnected()) {
                    synchronized(mViConnectionListeners) {
                        int i = mViConnectionListeners.beginBroadcast();
                        while(i > 0) {
                            i--;
                            try {
                                mViConnectionListeners.getBroadcastItem(i).onDisconnected();
                            } catch(RemoteException e) {
                                Log.w(TAG, "Couldn't notify VI connection " +
                                        "listener -- did it crash?", e);
                            }
                        }
                        mViConnectionListeners.finishBroadcast();
                    }
                }
            }
        }
    }
}
