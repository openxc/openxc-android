package com.openxc.messages.formatters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ByteAdapterTest {

    @Test
    public void testByteArrayToHex()  {
        assertEquals("00", ByteAdapter.byteArrayToHexString(new byte[] {0}));
        assertEquals("01", ByteAdapter.byteArrayToHexString(new byte[] {1}) );
        assertEquals("0C", ByteAdapter.byteArrayToHexString(new byte[] {12}));
        assertEquals("01020304", ByteAdapter.byteArrayToHexString(new byte[] {1, 2, 3, 4}));
        assertEquals("00010004", ByteAdapter.byteArrayToHexString(new byte[] {0, 1, 0, 4}));
        assertEquals("0C0F000107090B",ByteAdapter.byteArrayToHexString(new byte[] {12, 15, 0, 1, 7, 9, 11}));
        assertEquals("641F032F160978", ByteAdapter.byteArrayToHexString(new byte[] {100, 31, 3, 47, 22, 9, 120}));
    }

    @Test
    public void testHexToByteArray()  {
        assertTrue(Arrays.equals(ByteAdapter.hexStringToByteArray("00"), new byte[] {0}));
        assertTrue(Arrays.equals(ByteAdapter.hexStringToByteArray("01"), new byte[] {1}));
        assertTrue(Arrays.equals(ByteAdapter.hexStringToByteArray("0C"), new byte[] {12}));
        assertTrue(Arrays.equals(ByteAdapter.hexStringToByteArray("01020304"), new byte[] {1, 2, 3, 4}));
        assertTrue(Arrays.equals(ByteAdapter.hexStringToByteArray("00010004"), new byte[] {0, 1, 0, 4}));
        assertTrue(Arrays.equals(ByteAdapter.hexStringToByteArray("0C0F000107090B"), new byte[] {12, 15, 0, 1, 7, 9, 11}));
        assertTrue(Arrays.equals(ByteAdapter.hexStringToByteArray("641F032F160978"), new byte[] {100, 31, 3, 47, 22, 9, 120}));
    }

}
