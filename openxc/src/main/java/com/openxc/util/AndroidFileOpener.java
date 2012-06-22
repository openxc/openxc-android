package com.openxc.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.openxc.VehicleManager;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * A file opener that uses Android's permission flags.
 *
 * This file opener will only work on an Android device.
 */
public class AndroidFileOpener implements FileOpener {
    private static final String TAG = "AndroidFileOpener";

    private Context mContext;

    public AndroidFileOpener(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    public BufferedWriter openForWriting(String name) throws IOException {
        Log.i(TAG, "Opening file " + name + " for writing");

        File path = Environment.getExternalStoragePublicDirectory(VehicleManager.recordingPath);
        File file = new File(path, name);
        try {
            if (!path.exists()) {
                path.mkdirs();
            }

            OutputStream outputStream = new FileOutputStream(file);
            return new BufferedWriter(new OutputStreamWriter(outputStream));
        } catch(IOException e) {
            Log.w(TAG, "Unable to open " + name + " for writing", e);
            throw e;
        }
    }
}
