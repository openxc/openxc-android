package com.openxc.enabler.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.openxc.VehicleManager;

public abstract class VehiclePreferenceManager {
    private Context mContext;
    private PreferenceListener mPreferenceListener;
    private SharedPreferences mPreferences;
    private VehicleManager mVehicle;

    public VehiclePreferenceManager(Context context) {
        mContext = context;

        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void setVehicleManager(VehicleManager vehicle) {
        mVehicle = vehicle;
        mPreferenceListener = watchPreferences(mPreferences);
        mPreferenceListener.readStoredPreferences();
    }

    public void close() {
        unwatchPreferences(mPreferences, mPreferenceListener);
    }

    protected SharedPreferences getPreferences() {
        return mPreferences;
    }

    protected String getPreferenceString(int id) {
        return mPreferences.getString(mContext.getString(id), null);
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

    protected abstract PreferenceListener createPreferenceListener(
            SharedPreferences preferences);

    protected abstract class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        SharedPreferences mPreferences;

        public PreferenceListener(SharedPreferences preferences) {
            mPreferences = preferences;
        }

        public abstract void readStoredPreferences();
    }

    private void unwatchPreferences(SharedPreferences preferences,
            PreferenceListener listener) {
        if(preferences != null && listener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    private PreferenceListener watchPreferences(SharedPreferences preferences) {
        if(preferences != null) {
            PreferenceListener listener = createPreferenceListener(preferences);
            preferences.registerOnSharedPreferenceChangeListener(listener);
            return listener;
        }
        return null;
    }
}
