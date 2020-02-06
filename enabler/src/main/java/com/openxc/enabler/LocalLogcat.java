package com.openxc.enabler;

import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LocalLogcat {

    private final static String TAG                 = LocalLogcat.class.getSimpleName();
    private final static String LOGFILE_DIRECTORY   = "/ZebraLogFile";

    private static Process mProcess = null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US);

    final static long SINGLE_LOG_FILE_DURATION = 1000 * 60 * 60 * 8;        // 8 Hours
    final static long KEEP_LOG_FILE_TIME = 1000 * 60 * 60 * 24 * 14;        // 14 Days in milliseconds

    public static void initLogcat() {
        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + LOGFILE_DIRECTORY );
            File logFile = new File( appDirectory, "logcat" + dateFormat.format(new Date()) + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Runtime.getRuntime().exec("logcat -c");
                mProcess = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            deleteOldLogFiles();

            // After 8 hours of running, start another logfile and
//            // remove out of date files
//            Timer timer = new Timer();
//            TimerTask countdown = new TimerTask() {
//                @Override
//                public void run() {
//                    if (mProcess != null) {
//                        mProcess.destroy();
//
//                        File logFile = new File( appDirectory, "logcat_" + dateFormat.format(new Date()) + ".txt" );
//                        try {
//                            mProcess = Runtime.getRuntime().exec("logcat -f " + logFile);
//                            deleteOldLogFiles();
//                        } catch (IOException exception) {
//                            exception.printStackTrace();
//                        }
//
//                    }
//                }
//            };
//            timer.schedule(countdown, 0, SINGLE_LOG_FILE_DURATION);

        } else if ( isExternalStorageReadable() ) {
            Log.e(TAG, "External Storage is Readable, but not Writable");
        } else {
            Log.e(TAG, "External Storage is not accessible");
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }

    private static void deleteOldLogFiles() {
        File appDirectory = new File( Environment.getExternalStorageDirectory() + LOGFILE_DIRECTORY );
        if (!appDirectory.exists()) {
            return;
        }

        long currentTimeMilli = System.currentTimeMillis();
        File[] files = appDirectory.listFiles();
        if (files == null) {
            return;
        }
        for (File logFile : files) {
            long lastmodified = logFile.lastModified();
            if (currentTimeMilli - lastmodified > KEEP_LOG_FILE_TIME) {
                // Delete old stale Log files
                Log.e(TAG, "Deleting stale file:" + logFile.toString());
                logFile.delete();
            }
        }

    }
}

