package com.openxc.messages.formatters;


//
//  MultiFrameStitcher
//
//      This class will stitch together a multi-frame messages
//      into a single larger message
//
// Logic in BytestreamDataSource.java
//      will read a stream of messages in and then execute
//      the callback handleMessage(message)
//
// Assumptions
//     1) Data will always arrive in order
//     2) Messages arrive in a JSON format with the top level JSON object, in a
//          partial Frame
//     3) Size refers to the number of characters of payload in the Partial
//
// The Message structure from Ja'mez email 1/6/2020
//    {"bus": 1,
//            "frame": 0,               // Required Multiframe field
//            "total_size" : 0,         // Required Multiframe field
//            "message_id": 1234,       // Required Multiframe field
//            "mode": 1,
//            "pid": 5,
//            "success": true/false,
//            "payload": "0x1234",
//            "value": 4660}

import android.util.Log;

public class MultiFrameStitcher {
    private static final String TAG = MultiFrameStitcher.class.getSimpleName();

    static int filledBytes;                // Total Number of bytes that have received valid data
    static int totalBytes;                 // Total size of final message when all payloads have been
                                           // received and constructed
    static int message_id = -1;             // Message Id of current in construction message
    static String message = "";            // Location where the message will be constructed

    public static boolean addFrame(int messageId, String payload, int totalSize) {
        if (message_id != messageId) {
            initializeFrame();
            totalBytes = totalSize;
            message_id = messageId;
        }

        message += payload;
        filledBytes += payload.length();

        if (filledBytes > totalBytes) {
            Log.e(TAG, "Received more data than expected");
            return true;
        } else if (filledBytes == totalBytes) {
            return true;
        }
        return false;
    }

    public static void initializeFrame() {
        message = "";
        filledBytes = 0;
        totalBytes = 0;
        message_id = -1;
    }

    public static void clear() {
        initializeFrame();
    }

    public static String getMessage() {
        if (!isMultiFrameMessageComplete()) {
            Log.e(TAG, "Incomplete message returned");
        }
        return message;
    }

    public static boolean isMultiFrameMessageComplete() {
        if (filledBytes >= totalBytes) {
            return true;
        }
        return false;
    }

}
