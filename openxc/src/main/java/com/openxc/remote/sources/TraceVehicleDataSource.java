package com.openxc.remote.sources;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;

import android.util.Log;

public class TraceVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "TraceVehicleDataSource";

    private URL mFilename;

    public TraceVehicleDataSource() {
        super();
    }

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        super(callback);
    }

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            URL filename) {
        super(callback);
        setFilename(filename);
    }

    public void setFilename(URL filename) {
        mFilename = filename;
    }

    public void trigger(String name, double value) {
        handleMessage(name, value);
    }

    public void trigger(String name, String value) {
        handleMessage(name, value);
    }

    public void run() {
        BufferedReader reader;
        try {
            reader = openFile(mFilename);
        } catch(FileNotFoundException e) {
            Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
            return;
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
        DataInputStream dataStream = new DataInputStream(stream);
        return new BufferedReader(new InputStreamReader(dataStream));
    }
}
