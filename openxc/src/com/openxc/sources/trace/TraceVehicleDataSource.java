package com.openxc.sources.trace;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.JsonFormatter;
import com.openxc.sources.ContextualVehicleDataSource;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.SourceCallback;

/**
 * A vehicle data source that reads measurements from a pre-recorded trace file.
 *
 * This class is primarily for testing - a pre-recorded trace of the output from
 * an OpenXC vehicle interface is played back line by line into the library.
 * Everything from the VehicleService on up the chain is identical to when
 * operating in a live vehicle.
 *
 * The trace file format is defined in the OpenXC message format specification:
 * https://github.com/openxc/openxc-message-format
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

    private boolean mTraceValid = false;
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

    /** Consider the trace source "connected" if it's running and at least 1
     * measurement was parsed successfully from the file.
     *
     * This will catch errors e.g. if the trace is totally corrupted, but it
     * won't give you any indication if it is partially corrupted.
     */
    @Override
    public boolean isConnected() {
        return mRunning && mTraceValid;
    }

    /**
     * Stop trace file playback and the playback thread.
     */
    @Override
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
    @Override
    public void run() {
        while(mRunning) {
            waitForCallback();
            Log.d(TAG, "Starting trace playback from beginning of " + mFilename);
            BufferedReader reader;
            try {
                reader = openFile(mFilename);
            } catch(DataSourceException e) {
                Log.w(TAG, "Couldn't open the trace file " + mFilename, e);
                break;
            }

            String line = null;
            long startingTime = System.currentTimeMillis();
            // In the future may want to support binary traces
            try {
                while(mRunning && (line = reader.readLine()) != null) {
                    VehicleMessage measurement;
                    try {
                        measurement = JsonFormatter.deserialize(line);
                    } catch(UnrecognizedMessageTypeException e) {
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
                    if(!mTraceValid) {
                        connected();
                        mTraceValid = true;
                    }
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

            disconnected();
            Log.d(TAG, "Restarting playback of trace " + mFilename);
            // Set this back to false so the VI shows as "disconnected" for
            // a second before reconnecting.
            mTraceValid = false;
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {}
        }
        disconnected();
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
        return MoreObjects.toStringHelper(this)
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

    @Override
    protected String getTag() {
        return TAG;
    }

    /**
     * Using the startingTime as the relative starting point, sleep this thread
     * until the next timestamp would occur.
     *
     * @param startingTime the relative starting time in milliseconds
     * @param timestamp the timestamp to wait for in milliseconds since the
     * epoch
     */
    private void waitForNextRecord(long startingTime, long timestamp) {
        if(mFirstTimestamp == 0) {
            mFirstTimestamp = timestamp;
            Log.d(TAG, "Storing " + timestamp + " as the first " +
                    "timestamp of the trace file");
        }
        long targetTime = startingTime + (timestamp - mFirstTimestamp);
        long sleepDuration = Math.max(targetTime - System.currentTimeMillis(), 0);
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
