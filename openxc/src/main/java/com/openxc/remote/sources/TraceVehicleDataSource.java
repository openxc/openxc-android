package com.openxc.remote.sources;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


import android.util.Log;

public class TraceVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "TraceVehicleDataSource";

    private InputStream mStream;

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            InputStream stream) throws VehicleDataSourceException {
        super(callback);
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

    public void run() {
        BufferedReader reader;
        reader = openFile(mStream);

        String line;
        try {
            while((line = reader.readLine()) != null) {
                parseJson(line);
            }
        } catch(IOException e) {
            Log.w(TAG, "An exception occured when reading the trace file " +
                    mStream, e);
        }
    }

    private static BufferedReader openFile(InputStream stream) {
        DataInputStream dataStream = new DataInputStream(stream);
        return new BufferedReader(new InputStreamReader(dataStream));
    }
}
