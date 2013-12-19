package com.openxc.enabler.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Chernyshov Yuriy
 * Date: 12/14/13
 * Time: 8:11 PM
 */
public class AppUtils {

    private static String TAG = "AppUtils";

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get application version name.", e);
        }
        return versionName;
    }

    public static int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get application version code.", e);
        }
        return versionCode;
    }

    public static String getCrittercismKey(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("crittercism_key");
            String key = readInputStream(inputStream).trim();
            Log.i(TAG, "Crittercism key: " + key);
            return key;
        } catch (IOException e) {
            Log.w(TAG, "No Crittercism key found, You must specify it in 'assets/crittercism_key' file.", e);
        }
        return "";
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder stream = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = inputStream.read(b)) != -1;) {
            stream.append(new String(b, 0, n));
        }
        return stream.toString();
    }
}