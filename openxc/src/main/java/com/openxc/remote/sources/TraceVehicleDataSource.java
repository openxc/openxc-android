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
    private URI mFilename;

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

        mFilename = filename;
        Log.d(TAG, "Starting new trace data source with trace file " +
                mFilename);
    }

    public void trigger(String name, double value) {
        handleMessage(name, value);
    }

    public void trigger(String name, String value) {
        handleMessage(name, value);
    }

    public void stop() {
        Log.d(TAG, "Stopping trace playback");
        mRunning = false;
    }

    public void run() {
        if(mCallback != null) {
            mRunning = true;
            Log.d(TAG, "Starting the trace playback because we have valid " +
                    "callback " + mCallback);
        }

        while(mRunning) {
            BufferedReader reader;
            try {
                reader = openFile(mFilename);
            } catch(VehicleDataSourceException e) {
                Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
                break;
            }
            Log.d(TAG, "Opening and playing back trace from " + reader);

            String line;
            try {
                while((line = reader.readLine()) != null) {
                    parseJson(line);
                }
            } catch(IOException e) {
                Log.w(TAG, "An exception occured when reading the trace " +
                        reader, e);
                break;
            } finally {
                try {
                    reader.close();
                } catch(IOException e) {
                    Log.w(TAG, "Couldn't even close the trace file", e);
                }
            }
        }
        Log.d(TAG, "Playback of trace " + mFilename + " is finished");
    }

    private BufferedReader openFile(URI filename)
            throws VehicleDataSourceException {
        FileInputStream stream;
        try {
            stream = new FileInputStream(filename.toURL().getFile());
        } catch(FileNotFoundException e) {
            throw new VehicleDataSourceException(
                "Couldn't open the trace file " + filename, e);
        } catch(MalformedURLException e) {
            throw new VehicleDataSourceException(
                "Couldn't open the trace file " + filename, e);
        }

        DataInputStream dataStream = new DataInputStream(stream);
        return new BufferedReader(new InputStreamReader(dataStream));
    }
}
