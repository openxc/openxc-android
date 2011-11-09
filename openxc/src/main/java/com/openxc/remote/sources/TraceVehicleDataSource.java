package com.openxc.remote.sources;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;

import android.util.Log;

public class TraceVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "TraceVehicleDataSource";

    private URL mFilename;
    private InputStream mStream;

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            URL filename) throws VehicleDataSourceException {
        super(callback);
        mFilename = filename;

        if(mFilename == null) {
            throw new VehicleDataSourceException(
                    "No filename specified for the trace source");
        }
    }

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

        if(mFilename != null) {
            try {
                reader = openFile(mFilename);
            } catch(FileNotFoundException e) {
                Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
                return;
            }
        } else {
                reader = openFile(mStream);
        }

        String line;
        try {
            while((line = reader.readLine()) != null) {
                parseJson(line);
            }
        } catch(IOException e) {
            Log.w(TAG, "An exception occured when reading the trace file " +
                    mFilename, e);
        }
    }

    private static BufferedReader openFile(URL filename)
            throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(filename.getFile());
        return openFile(stream);
    }

    private static BufferedReader openFile(InputStream stream) {
        DataInputStream dataStream = new DataInputStream(stream);
        return new BufferedReader(new InputStreamReader(dataStream));
    }
}
