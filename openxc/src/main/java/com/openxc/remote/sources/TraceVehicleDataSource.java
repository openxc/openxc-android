package com.openxc.remote.sources;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;

import android.util.Log;

public class TraceVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "TraceVehicleDataSource";

    private boolean mRunning;
    private URI mFilename;
    private InputStream mStream;

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        super(callback);
        mRunning = false;
    }

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            URI filename) throws VehicleDataSourceException {
        this(callback);
        mFilename = filename;

        if(mFilename == null) {
            throw new VehicleDataSourceException(
                    "No filename specified for the trace source");
        }
    }

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            InputStream stream) throws VehicleDataSourceException {
        this(callback);
        mStream = stream;

        if(mStream == null) {
            throw new VehicleDataSourceException(
                    "No input stream specified for the trace source");
        }
    }

    public void trigger(String name, double value) {
        handleMessage(name, value);
    }

    public void trigger(String name, String value) {
        handleMessage(name, value);
    }

    public void stop() {
        mRunning = false;
    }

    public void run() {
        if(mCallback != null) {
            mRunning = true;
        }

        InputStream stream = null;
        if(mFilename != null) {
            try {
                stream = new FileInputStream(mFilename.toURL().getFile());
            } catch(FileNotFoundException e) {
                Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
                return;
            } catch(MalformedURLException e) {
                Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
                return;
            }
        } else {
            stream = mStream;
        }

        while(mRunning) {
            BufferedReader reader = openFile(stream);

            String line;
            try {
                while((line = reader.readLine()) != null) {
                    parseJson(line);
                }
            } catch(IOException e) {
                Log.w(TAG, "An exception occured when reading the trace file " +
                        mFilename, e);
                try {
                    stream.close();
                } catch(IOException ee) {
                    Log.w(TAG, "Couldn't even close the trace file", ee);
                }
                return;
            }
        }

        try {
            if(stream != null) {
                stream.close();
            }
        } catch(IOException e) {
            Log.w(TAG, "Couldn't even close the trace file", e);
        }
    }

    private static BufferedReader openFile(InputStream stream) {
        DataInputStream dataStream = new DataInputStream(stream);
        return new BufferedReader(new InputStreamReader(dataStream));
    }
}
