package com.openxc.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;

import android.content.Context;

import android.util.Log;

public class AndroidFileOpener implements FileOpener {
    private static final String TAG = "AndroidFileOpener";

    private Context mContext;

    public AndroidFileOpener(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }

    public BufferedWriter openForWriting(String path) throws IOException {
        Log.i(TAG, "Opening file " + path + " for writing");
        try {
            OutputStream outputStream = getContext().openFileOutput(path,
                    Context.MODE_WORLD_READABLE | Context.MODE_APPEND);
            return new BufferedWriter(new OutputStreamWriter(outputStream));
        } catch(IOException e) {
            Log.w(TAG, "Unable to open " + path + " for writing", e);
            throw e;
        }
    }
}
