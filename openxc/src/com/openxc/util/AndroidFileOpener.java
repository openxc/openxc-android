package com.openxc.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

/**
 * A file opener that uses Android's permission flags.
 *
 * This file opener will only work on an Android device.
 */
public class AndroidFileOpener implements FileOpener {
    private static final String TAG = "AndroidFileOpener";
    private static final String DEFAULT_DIRECTORY = "openxc/traces";

    private String mDirectory;

    public AndroidFileOpener(String directory) {
        mDirectory = directory;
    }

    @Override
    public BufferedWriter openForWriting(String filename) throws IOException {
        Log.i(TAG, "Opening " + getDirectory() + "/" + filename
                + " for writing on external storage");

        File externalStoragePath = Environment.getExternalStorageDirectory();
        File directory = new File(externalStoragePath.getAbsolutePath() +
                "/" + getDirectory());
        File file = new File(directory, filename);
        try {
            directory.mkdirs();
            OutputStream outputStream = new FileOutputStream(file);
            return new BufferedWriter(new OutputStreamWriter(outputStream));
        } catch(IOException e) {
            Log.w(TAG, "Unable to open " + file + " for writing", e);
            throw e;
        }
    }

    private String getDirectory() {
        if(mDirectory == null) {
            return DEFAULT_DIRECTORY;
        };
        return mDirectory;
    }
}
