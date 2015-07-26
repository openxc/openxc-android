package com.openxc.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.common.base.Joiner;

public class SupportSettingsUtils {

    private static boolean supportsStringSet() {
        return android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Retrieve a set of strings from SharedPreferences, using the built-in
     * getStringSet method if available and falling back to a comma separated
     * String if not.
     *
     * Note that this is a simple approach that won't work if the values in the
     * set contain commas.
     *
     * @param preferences the SharedPreferences to retrieve the set from.
     * @param key the key for the preference.
     * @param defaultValue the default value to return if the preference is not
     *      stored.
     *
     * @return The set if found, otherwise the default value
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(SharedPreferences preferences,
            String key, Set<String> defaultValue) {
        Set<String> result = defaultValue;
        if(supportsStringSet()) {
            result = preferences.getStringSet(key, defaultValue);
        } else {
            String serializedSet = preferences.getString(key, "");
            // Don't use String.isEmpty() - it's only available in API 9 and
            // greater and we want to support 8
            if(serializedSet.length() > 0) {
                result = new HashSet<>(Arrays.asList(
                            serializedSet.split(",")));
            }
        }
        return result;
    }

    /**
     *
     * Store a set in SharedPreferences, using the built-in putStringSet method
     * if available and falling back to a comma separated String if not.
     *
     *
     * Note that this is a simple approach that won't work if the values in the
     * set contain commas.
     *
     * @param editor the editor to store the set in.
     * @param key the key for the preference.
     * @param value the value to store with the key.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void putStringSet(SharedPreferences.Editor editor,
            String key, Set<String> value) {
        if(supportsStringSet()) {
            editor.putStringSet(key, value);
        } else {
            editor.putString(key, Joiner.on(",").join(value));
        }
    }
}
