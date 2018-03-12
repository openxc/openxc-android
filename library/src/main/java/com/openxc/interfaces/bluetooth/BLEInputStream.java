package com.openxc.interfaces.bluetooth;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by AKUMA128 on 2/9/2018.
 */

public class BLEInputStream extends InputStream {
    public static final int MAX_READ_BUFFER_CAPACITY = 4096;
    private static final String TAG = BLEInputStream.class.getSimpleName();
    private static BLEInputStream bleInputStream = null;

    private ByteBuffer buf = ByteBuffer.allocate(MAX_READ_BUFFER_CAPACITY);

    public static BLEInputStream getInstance() {
        if (bleInputStream == null) {
            bleInputStream = new BLEInputStream();
        }
        return bleInputStream;
    }

    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    public synchronized int read(byte[] bytes, int off, int len)
            throws IOException {
        buf.flip();
        int bufferLength = buf.limit();
        try {
            buf.get(bytes, off, bufferLength);
        } catch (BufferUnderflowException e) {
            Log.d(TAG, "BufferUnderflow bufferLength : " + bufferLength);
        }
        buf.clear();
        return bufferLength;
    }

    public synchronized void putDataInBuffer(byte[] data) {
        try {
            buf.put(data);
        } catch (BufferOverflowException e) {
            Log.e(TAG, "Buffer overflowing Resetting!!!!");
            buf.clear();
        }
    }

    public boolean doesBufferHasRemaining() {
        return buf.position() > 0;
    }

    public void clearBuffer() {
        buf.clear();
    }
}
