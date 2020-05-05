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

import android.util.Log;

public class MultiFrameStitcher {
    private static final String TAG = MultiFrameStitcher.class.getSimpleName();

    static int filledBytes;                // Total Number of bytes that have received valid data
    static int totalBytes;                 // Total size of final message when all payloads have been
                                           // received and constructed
    static int frame;
    static int message_id = -1;             // Message Id of current in construction message
    static String message = "";            // Location where the message will be constructed

    private MultiFrameStitcher (){

    }

    public static boolean addFrame(int messageId, int frameValue, String payload, int totalSize) {

        // If message is different than last then initialize
        // Also if previous frame was -1 then initialize as well
        if ((message_id != messageId) || (frame == -1)) {
            initializeFrame();
            totalBytes = totalSize;
            message_id = messageId;
        }

        if  ((message.length() > 0) && (payload.substring(0,2).compareToIgnoreCase("0x") == 0)) {
            message += payload.substring(2);
            filledBytes += payload.length()-2;
        } else {
            message += payload;
            filledBytes += payload.length();
        }
        frame = frameValue;

        if (frame == -1) {  // Last frame is marked with a "-1"
            Log.d(TAG, "Received Last frame of a multi-frame");
            return true;
        }
        return false;
    }

    public static void initializeFrame() {
        message = "";
        filledBytes = 0;
        totalBytes = 0;
        message_id = -1;
        frame = 0;
    }

    public static void clear() {
        initializeFrame();
    }

    public static String getMessage() {
        if (!isMultiFrameMessageComplete()) {
            Log.e(TAG, "Incomplete message returned");
        }
        Log.e(TAG, "Getting message for message_id:" + message_id);
        Log.e(TAG, message);
        return message;
    }

    public static boolean isMultiFrameMessageComplete() {
        if ((totalBytes > 0) && (filledBytes >= totalBytes)) {
            return true;
        } else if (frame == -1) {  // Last frame is marked with a "-1"
            return true;
        }
        return false;
    }

}
