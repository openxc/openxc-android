package com.openxc.remote.sources.trace;

import java.util.concurrent.TimeUnit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import android.content.res.Resources;

import android.util.Log;

/**
 * A vehicle data source that reads measurements from a pre-recorded trace file.
 *
 * This class is primarily for testing - a pre-recorded trace of the output from
 * an OpenXC CAN translator is played back line by line into the library.
 * Everything from the RemoteVehicleService on up the chain is identical to when
 * operating in a live vehicle.
 *
 * The expected format of the trace file is UNIX timestamps followed by JSON
 * objects, separated by newlines:
 *
 * 1332794184.319404: {"name":"fuel_consumed_since_restart","value":0.090000}
 * 1332794184.502802: {"name":"steering_wheel_angle","value":-346.985229}
 * 1332794184.559463: {"name":"powertrain_torque","value":1.000000}
 *
 * The trace file to use is specified via the constructor as an Android-style
 * resource URI, e.g. "resource://42" or a plain file path
 * (e.g. "file:///sdcard/com.openxc/trace.json" ). When using resources, the ID
 * for the resource is accessed through the generated "R.java" file. For
 * example:
 *
 *      URI resource = new URI("resource://" + R.raw.trace)
 *
 * where the trace file is located at res/raw/trace.
 *
 * Regular files are the preferred way to use trace files, and are required when
 * using the OpenXC enabler app. Android apps don't have access to each others'
 * raw resources, so the resource method will not work in that case.
 *
 * The trace file is played back in a continuous loop at roughly the same speed
 * as the original recording (at least according to the timestamps in the file).
 *
 * Playback will not begin until a callback is set, either via a constructor or
 * the {@link com.openxc.remote.sources.AbstractVehicleDataSource#setCallback(VehicleDataSourceCallbackInterface)}
 * function.
 */
public class TraceVehicleDataSource extends JsonVehicleDataSource {
    private static final String TAG = "TraceVehicleDataSource";

    private Long mFirstTimestamp;
    private boolean mRunning;
    private URI mFilename;

    /** Construct a trace data source with the given context and callback.
     *
     * If the callback is not null, playback will begin immediately.
     *
     * TODO Does this explode if you don't set a filename? It might, doesn't
     * look like we check that in run(). It might just continuously try to open
     * null.
     *
     * @param context the Activity or Service context, used to access the raw
     *      trace file resource via Android.
     * @param callback An object implementing the
     *      VehicleDataSourceCallbackInterface that should receive data as it is
     *      received and parsed.
     */
    public TraceVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback) {
        super(context, callback);
        mRunning = false;
    }

    /** Construct a trace data source with the given callback and a custom
     * filename.
     *
     * TODO I don't think we need this anymore, and I think it wouldn't work
     * anyway - we require a context to open the files. This is a remnant of
     * when we accepted absolute paths to files on the SD card, for example.
     */
    public TraceVehicleDataSource(
            VehicleDataSourceCallbackInterface callback,
            URI filename) throws VehicleDataSourceException {
        this(null, callback, filename);
    }

    /** Construct a trace data source with the given context, callback and
     * trace file resource URI.
     *
     * @param context the Activity or Service context, used to access the raw
     *      trace file resource via Android.
     * @param callback An object implementing the
     *      VehicleDataSourceCallbackInterface that should receive data as it is
     *      received and parsed.
     * @param filename a raw file resource URI of the format
     *          "resource://resource_id"
     * @throws VehicleDataSourceException  if no filename is specified
     */
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

    /**
     * Stop trace file playback and the playback thread.
     */
    public void stop() {
        Log.d(TAG, "Stopping trace playback");
        mRunning = false;
    }

    /** While running, continuously read from the trace file and send messages
     * to the callback.
     *
     * If the callback is not set, this function will exit immediately and the
     * thread will die a quick death.
     *
     * TODO why do we let you not set a callback anyway? can that be deprecated?
     */
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

            String line = null;
            long startingTime = System.nanoTime();
            try {
                while((line = reader.readLine()) != null) {
                    String[] record = line.split(":", 2);
                    if(record.length != 2) {
                        Log.w(TAG, "A trace line was not in the expected " +
                                "format: " + line);
                        continue;
                    }

                    try {
                        waitForNextRecord(startingTime,
                                Double.parseDouble(record[0]));
                    } catch(NumberFormatException e) {
                        Log.w(TAG, "A trace line was not in the expected " +
                                "format: " + line);
                        continue;
                    }
                    handleJson(record[1]);
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
            Log.d(TAG, "Restarting playback of trace " + mFilename);
        }
        Log.d(TAG, "Playback of trace " + mFilename + " is finished");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("filename", mFilename)
            .add("callback", getCallback())
            .toString();
    }

    private void waitForNextRecord(long startingTime, double timestampSeconds) {
        long timestamp = TimeUnit.NANOSECONDS.convert(
                (long)(timestampSeconds * 1000), TimeUnit.MILLISECONDS);
        if(mFirstTimestamp == null) {
            mFirstTimestamp = new Long(timestamp);
            Log.d(TAG, "Storing " + mFirstTimestamp + " as the first " +
                    "timestamp of the trace file");
        }
        long targetTime = startingTime + (timestamp - mFirstTimestamp);
        long sleepDuration = TimeUnit.MILLISECONDS.convert(
                targetTime - System.nanoTime(), TimeUnit.NANOSECONDS);
        sleepDuration = Math.max(sleepDuration, 0);
        try {
            Thread.sleep(sleepDuration);
        } catch(InterruptedException e) {}
    }

    private BufferedReader openResourceFile(URI filename) {
        InputStream stream;
        try {
            stream = getContext().getResources().openRawResource(
                    new Integer(filename.getAuthority()));
        } catch(Resources.NotFoundException e) {
            Log.w(TAG, "Unable to find a trace resource with URI " + filename
                    + " -- returning an empty buffer");
            stream = new ByteArrayInputStream(new byte[0]);
        }
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
}
