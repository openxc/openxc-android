package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.openxc.VehicleManager;
import java.lang.ref.WeakReference;

/**
 * Abstract base class that collects functionality common to watching shared
 * preferences for changes and altering running services as a result.
 *
 * The preference listeners for each specific group of preferences can be
 * contained in a subclass, instead of all cluttering up the main activity.
 */
public abstract class VehiclePreferenceManager {
    private final static String TAG = "VehiclePreferenceManager";
    private Context mContext;
    private PreferenceListener mPreferenceListener;
    private SharedPreferences mPreferences;
    private VehicleManager mVehicle;

    public VehiclePreferenceManager(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
    }

    /**
     * Give the instance a reference to an active VehicleManager.
     */
    public void setVehicleManager(VehicleManager vehicle) {
        mVehicle = vehicle;
        mPreferenceListener = watchPreferences(getPreferences());
        if (mPreferenceListener != null) {
            mPreferenceListener.readStoredPreferences();
        } else {
            Log.w(TAG, "mPreferenceListener was null");
        }
    }

    /**
     * Shutdown any running services and stop watching the shared preferences.
     */
    public void close() {
        unwatchPreferences(getPreferences(), mPreferenceListener);
    }

    protected SharedPreferences getPreferences() {
        return mPreferences;
    }

    protected String getPreferenceString(int id) {
        return getPreferences().getString(mContext.getString(id), null);
    }

    protected String getString(int id) {
        return mContext.getString(id);
    }

    protected Context getContext() {
        return mContext;
    }

    protected VehicleManager getVehicleManager() {
        return mVehicle;
    }

    /**
     * Return an instance of a PreferenceListener implementation, defined by the
     * subclass.
     */
    protected abstract PreferenceListener createPreferenceListener();

    protected static abstract class PreferenceListener implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        /**
         * Re-read shared preferences and update any running services.
         *
         * This method will be called whenever the value of any of this
         * PreferenceListener's watched preferences changes.
         */
        protected abstract void readStoredPreferences();

        /**
         * Return an array of string resource IDs that correspond to the
         * preference keys that should be monitored for changes.
         */
        protected abstract int[] getWatchedPreferenceKeyIds();
        /**
         * Reference to enclosing class.
         */
        private final WeakReference<VehiclePreferenceManager> mReference;

        /**
         * Default constructor.
         *
         * @param reference Reference to enclosing class.
         */
        protected PreferenceListener(final VehiclePreferenceManager reference) {
            super();
            mReference = new WeakReference<>(reference);
        }

        /**
         * Returns a reference to the enclosing class.
         *
         * @return A reference to the enclosing class or {@code null} if a reference is
         * garbage collected.
         */
        protected VehiclePreferenceManager getEnclosingReference() {
            return mReference.get();
        }


        /**
         * If any of the watched preference keys changed, trigger a refresh of
         * the service.
         */
        public void onSharedPreferenceChanged(SharedPreferences preferences,
                                              String key) {
            final VehiclePreferenceManager reference = getEnclosingReference();
            if (reference == null) {
                Log.w(TAG, "Can not handle shared preferenced changes, enclosing reference is null");
                return;
            }

            for(int watchedKeyId : getWatchedPreferenceKeyIds()) {
                if(key.equals(reference.getString(watchedKeyId))) {
                    readStoredPreferences();
                    break;
                }
            }
        }
    }
       /* public void onSharedPreferenceChanged(SharedPreferences preferences,
                String key) {
            for(int watchedKeyId : getWatchedPreferenceKeyIds()) {
                if(key.equals(getString(watchedKeyId))) {
                    readStoredPreferences();
                    break;
                }
            }
        }
    }*/

    private void unwatchPreferences(SharedPreferences preferences,
            PreferenceListener listener) {
        if(preferences != null && listener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    private PreferenceListener watchPreferences(SharedPreferences preferences) {
        if(preferences != null) {
            PreferenceListener listener = createPreferenceListener();
            preferences.registerOnSharedPreferenceChangeListener(listener);
            return listener;
        }
        return null;
    }
}
