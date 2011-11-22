package com.openxc.remote.sources.trace;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;

import com.google.common.base.Objects;

import com.openxc.remote.sources.JsonVehicleDataSource;
import com.openxc.remote.sources.VehicleDataSourceCallbackInterface;
import com.openxc.remote.sources.VehicleDataSourceException;

import android.content.Context;

import android.util.Log;

public class TraceVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "TraceVehicleDataSource";

    private boolean mRunning;
    private URI mFilename;

    public TraceVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback) {
        super(context, callback);
        mRunning = false;
    }

    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            URI filename) throws VehicleDataSourceException {
        this(null, callback, filename);
    }

    public TraceVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback,
            URI filename) throws VehicleDataSourceException {
        this(context, callback);
        if(filename == null) {
            throw new VehicleDataSourceException(
                    "No filename specified for the trace source");
        }

        mFilename = filename;
        Log.d(TAG, "Starting new trace data source with trace file " +
                mFilename);
    }

    public void stop() {
        Log.d(TAG, "Stopping trace playback");
        mRunning = false;
    }

    public void run() {
        if(getCallback() != null) {
            mRunning = true;
            Log.d(TAG, "Starting the trace playback because we have valid " +
                    "callback " + getCallback());
        }

        while(mRunning) {
            BufferedReader reader;
            try {
                reader = openFile(mFilename);
            } catch(VehicleDataSourceException e) {
                Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
                break;
            }

            String line;
            try {
                while((line = reader.readLine()) != null) {
                    handleJson(line);
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

    private BufferedReader openResourceFile(URI filename)
            throws VehicleDataSourceException {
        InputStream stream;
        stream = getContext().getResources().openRawResource(
                new Integer(filename.getAuthority()));
        return readerForStream(stream);
    }

    private BufferedReader openRegularFile(URI filename)
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

        return readerForStream(stream);
    }

    private BufferedReader readerForStream(InputStream stream) {
        DataInputStream dataStream = new DataInputStream(stream);
        return new BufferedReader(new InputStreamReader(dataStream));
    }

    private BufferedReader openFile(URI filename)
            throws VehicleDataSourceException {
        if(filename.getScheme().equals("resource")) {
            return openResourceFile(filename);
        } else {
            return openRegularFile(filename);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("filename", mFilename)
            .add("callback", getCallback())
            .toString();
    }
}
