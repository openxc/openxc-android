package com.openxc.messages;

import android.os.Parcel;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.openxc.messages.formatters.MultiFrameStitcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//
// At the moment there is no offset for the data in the multi-frame
// message, so we can only assume that the messages come in order and
// are not clipped.
// If a Multi-Frame message is duplicated unfortunately, it will be
// appended twice or more to the accumulated payload.
//

public class MultiFrameResponse extends KeyedMessage {

    private static final String TAG = MultiFrameResponse.class.getSimpleName();

    private static final String FRAME_KEY = "frame";
    private static final String MESSAGE_ID_KEY = "message_id";
    private static final String TOTAL_SIZE_KEY = "total_size";
    private static final String SIZE_KEY = "size";
    private static final String PAYLOAD_KEY = "payload";

    private static final String[] sRequiredFieldsValues = new String[] {
            FRAME_KEY, TOTAL_SIZE_KEY, MESSAGE_ID_KEY };
    private static final Set<String> sRequiredFields = new HashSet<>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(FRAME_KEY)
    private int mFrame;

    @SerializedName(TOTAL_SIZE_KEY)
    private int mTotalSize;

    @SerializedName(MESSAGE_ID_KEY)
    private int mMessageId;

    // Optional Fields

    @SerializedName(SIZE_KEY)
    private int mSize;

    @SerializedName(PAYLOAD_KEY)
    private String mPayload;

    public int getFrame() {
        return mFrame;
    }

    public int getTotalSize() {
        return mTotalSize;
    }

    public int getMessageId() {
        return mMessageId;
    }

    public int getSize() {
        return mSize;
    }

    public String getPayload() {
        return mPayload;
    }

    // addSequentialData
    //      returns True if our assembled message is complete
    public boolean addSequentialData() {
        return MultiFrameStitcher.addFrame(mMessageId, mPayload, mTotalSize);
    }

    public void clear() {
        MultiFrameStitcher.clear();
    }

    public String getAssembledMessage() {
        Log.e(TAG, "message_id:" + mMessageId);
        Log.e(TAG, "total_size:" + mTotalSize);
        Log.e(TAG, "frame:" + mFrame);
        Log.e(TAG, "payload length:" + mPayload.length());
        Log.e(TAG, "Payload:" + mPayload);

        // Get the Message from the stitcher
        return MultiFrameStitcher.getMessage();
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(mFrame);
        out.writeInt(mTotalSize);
        out.writeInt(mMessageId);
        out.writeInt(mSize);
        out.writeString(getPayload());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);

        mFrame = in.readInt();
        mTotalSize = in.readInt();
        mMessageId = in.readInt();
        mSize = in.readInt();
        mPayload = in.readString();
    }

    protected MultiFrameResponse(Parcel in) {
        readFromParcel(in);
    }

    protected MultiFrameResponse() {}

    public MultiFrameResponse(int frame, int totalSize, int messageId, int size, String payload) {
        mFrame = frame;
        mTotalSize = totalSize;
        mMessageId = messageId;
        mSize = size;
        mPayload = payload;
    }
}
