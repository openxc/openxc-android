package com.openxc.remote.sources;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;

import android.util.Log;

public class TraceVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "TraceVehicleDataSource";

    private boolean mRunning;
    private FileInputStream mStream;

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        super(callback);
        mRunning = false;
    }

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            URI filename) throws VehicleDataSourceException {
        this(callback);
        if(filename == null) {
            throw new VehicleDataSourceException(
                    "No filename specified for the trace source");
        }
        try {
            mStream = new FileInputStream(filename.toURL().getFile());
        } catch(FileNotFoundException e) {
            throw new VehicleDataSourceException(
                "Couldn't open the trace file " + filename, e);
        } catch(MalformedURLException e) {
            throw new VehicleDataSourceException(
                "Couldn't open the trace file " + filename, e);
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
        BufferedReader reader;

        if(mCallback != null) {
            mRunning = true;
        }

        while(mRunning) {
            DataInputStream dataStream = new DataInputStream(mStream);
            reader = new BufferedReader(new InputStreamReader(dataStream));

            String line;
            try {
                while((line = reader.readLine()) != null) {
                    parseJson(line);
                }
            } catch(IOException e) {
                Log.w(TAG, "An exception occured when reading the trace " +
                        mStream, e);
            } finally {
                try {
                    reader.close();
                } catch(IOException e) {
                    Log.w(TAG, "Couldn't even close the trace file", e);
                }
            }
        }
    }
}
