package com.openxc.sources.trace;

import com.google.common.base.Objects;

import java.util.concurrent.TimeUnit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URI;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.sources.ContextualVehicleDataSource;
import com.openxc.sources.SourceCallback;
import com.openxc.sources.DataSourceException;

import com.openxc.remote.RawMeasurement;

import android.content.Context;
import android.content.res.Resources;

import android.util.Log;

/**
 * A vehicle data source that reads measurements from a pre-recorded trace file.
 *
 * This class is primarily for testing - a pre-recorded trace of the output from
 * an OpenXC vehicle interface is played back line by line into the library.
 * Everything from the VehicleService on up the chain is identical to when
 * operating in a live vehicle.
 *
 * The trace file format is simply a plain text file of OpenXC JSON messages with
 * an additional timestamp field (in seconds with decimal parts), separated by
 * newlines:
 *
 * {"timestamp": 1351176963.426318, "name": "door_status", "value": "passenger", "event": true}
 * {"timestamp": 1351176963.438087, "name": "fine_odometer_since_restart", "value": 0.0}
 * {"timestamp": 1351176963.438211, "name": "brake_pedal_status", "value": false}
 * {"timestamp": 1351176963.438318, "name": "transmission_gear_position", "value": "second"}
 *
 * The trace file to use is specified via the constructor as an Android-style
 * resource URI, e.g. "resource://42", "file:///storage/traces/trace.json" or a
 * plain file path (e.g. "/sdcard/com.openxc/trace.json" ). When using
 * resources, the ID for the resource is accessed through the generated "R.java"
 * file. For example:
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
 * the
 * {@link com.openxc.sources.BaseVehicleDataSource#setCallback(SourceCallback)}
 * function.
 */
public class TraceVehicleDataSource extends ContextualVehicleDataSource
            implements Runnable {
    private static final String TAG = "TraceVehicleDataSource";

    private long mFirstTimestamp = 0;
    private boolean mRunning = true;
    private boolean mLoop = true;
    private URI mFilename;

    /** Construct a trace data source with the given context, callback and
     * trace file resource URI.
     *
     * If the callback is not null, playback will begin immediately.
     *
     * @param context the Activity or Service context, used to access the raw
     *      trace file resource via Android.
     * @param callback An object implementing the SourceCallback interface that
     *      should receive data as it is received and parsed.
     * @param filename a raw file resource URI of the format
     *          "resource://resource_id"
     * @throws DataSourceException  if no filename is specified
     */
    public TraceVehicleDataSource(SourceCallback callback, Context context,
            URI filename) throws DataSourceException {
        this(callback, context, filename, true);
    }

    public TraceVehicleDataSource(SourceCallback callback, Context context,
            URI filename, boolean loop) throws DataSourceException {
        super(callback, context);
        if(filename == null) {
            throw new DataSourceException(
                    "No filename specified for the trace source");
        }

        mFilename = filename;
        mLoop = loop;
        Log.d(TAG, "Starting new trace data source with trace file " +
                mFilename);
        new Thread(this).start();
    }

    public TraceVehicleDataSource(Context context, URI filename)
            throws DataSourceException {
        this(null, context, filename);
    }

    public TraceVehicleDataSource(Context context, String filename)
            throws DataSourceException {
        this(null, context, uriFromString(filename));
    }

    public TraceVehicleDataSource(Context context, URI filename, boolean loop)
            throws DataSourceException {
        this(null, context, filename, loop);
    }

    @Override
    public boolean isConnected() {
        // TODO BaseVehicleDataSource.isConnected() always returns false do
    	// not us its result until this is fixed
    	//return mRunning && super.isConnected();
    	
    	return mRunning;
    }

    /**
     * Stop trace file playback and the playback thread.
     */
    public void stop() {
        super.stop();
        Log.d(TAG, "Stopping trace playback");
        mRunning = false;
    }

    /**
     * While running, continuously read from the trace file and send messages
     * to the callback.
     *
     * If the callback is not set, this function will exit immediately and the
     * thread will die a quick death.
     */
    public void run() {
        while(mRunning) {
            Log.d(TAG, "Starting trace playback from beginning of " + mFilename);
            BufferedReader reader;
            try {
                reader = openFile(mFilename);
            } catch(DataSourceException e) {
                Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
                break;
            }

            String line = null;
            long startingTime = System.nanoTime();
            try {
                while(mRunning && (line = reader.readLine()) != null) {
                    RawMeasurement measurement;
                    try {
                        measurement = new RawMeasurement(line);
                    } catch(UnrecognizedMeasurementTypeException e) {
                        Log.w(TAG, "A trace line was not in the expected " +
                                "format: " + line);
                        continue;
                    }

                    if(measurement != null && !measurement.isTimestamped()) {
                        Log.w(TAG, "A trace line was missing a timestamp: " +
                                line);
                        continue;
                    }

                    try {
                        waitForNextRecord(startingTime,
                                measurement.getTimestamp());
                    } catch(NumberFormatException e) {
                        Log.w(TAG, "A trace line was not in the expected " +
                                "format: " + line);
                        continue;
                    }
                    measurement.untimestamp();
                    handleMessage(measurement);
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

            if(!mLoop) {
                Log.d(TAG, "Not looping trace.");
                break;
            }
            Log.d(TAG, "Restarting playback of trace " + mFilename);
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {}
        }
        mRunning = false;
        Log.d(TAG, "Playback of trace " + mFilename + " is finished");
    }

    private static URI uriFromString(String path) throws DataSourceException {
        try {
            return new URI(path);
        } catch(java.net.URISyntaxException e) {
            throw new DataSourceException("Trace file path not valid", e);
        }
    }

    public static boolean validatePath(String path) {
        if(path == null) {
            Log.w(TAG, "Trace file path not set (it's " + path + ")");
            return false;
        }

        try {
            uriFromString(path);
            return true;
        } catch(DataSourceException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("filename", mFilename)
            .toString();
    }

    public boolean sameFilename(String filename) {
        try {
            return mFilename.equals(uriFromString(filename));
        } catch(DataSourceException e) {
            return false;
        }

    }

    protected String getTag() {
        return TAG;
    }

    /**
     * Using the startingTime as the relative starting point, sleep this thread
     * until the next timestamp would occur.
     *
     * @param startingTime the relative starting time in nanoseconds
     * @param timestamp the timestamp to wait for in milliseconds since the
     * epoch
     */
    private void waitForNextRecord(long startingTime, long timestamp) {
        // TODO is all of this nanosecond conversion necessary? the original
        // motivation was to get the most accurate time from the system, but i"m
        // not really sure it matters if the records are only timestamped at
        // the ms level.
        long timestampNanoseconds = TimeUnit.NANOSECONDS.convert(
                timestamp, TimeUnit.MILLISECONDS);
        if(mFirstTimestamp == 0) {
            mFirstTimestamp = timestampNanoseconds;
            Log.d(TAG, "Storing " + timestamp + " as the first " +
                    "timestamp of the trace file");
        }
        long targetTime = startingTime + (timestampNanoseconds - mFirstTimestamp);
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
                    Integer.valueOf(filename.getAuthority()));
        } catch(Resources.NotFoundException e) {
            Log.w(TAG, "Unable to find a trace resource with URI " + filename
                    + " -- returning an empty buffer");
            stream = new ByteArrayInputStream(new byte[0]);
        }
        return readerForStream(stream);
    }

    private BufferedReader openRegularFile(URI filename)
            throws DataSourceException {
        FileInputStream stream;
        try {
            stream = new FileInputStream(filename.getPath());
        } catch(FileNotFoundException e) {
            throw new DataSourceException(
                "Couldn't open the trace file " + filename, e);
        }

        return readerForStream(stream);
    }

    private BufferedReader readerForStream(InputStream stream) {
        DataInputStream dataStream = new DataInputStream(stream);
        return new BufferedReader(new InputStreamReader(dataStream));
    }

    private BufferedReader openFile(URI filename)
            throws DataSourceException {
        String scheme = filename.getScheme();
        if(scheme != null && scheme.equals("resource")) {
            return openResourceFile(filename);
        } else {
            return openRegularFile(filename);
        }
    }
}
