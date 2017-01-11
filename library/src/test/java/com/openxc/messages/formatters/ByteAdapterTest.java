package com.openxc.messages.formatters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.messages.formatters.ByteAdapter;

@RunWith(RobolectricTestRunner.class)
public class ByteAdapterTest {

    @Test
    public void testByteArrayToHex()  {
        assertEquals(ByteAdapter.byteArrayToHexString(new byte[] {0}), "00");
        assertEquals(ByteAdapter.byteArrayToHexString(new byte[] {1}), "01");
        assertEquals(ByteAdapter.byteArrayToHexString(new byte[] {12}), "0C");
        assertEquals(ByteAdapter.byteArrayToHexString(new byte[] {1, 2, 3, 4}), "01020304");
        assertEquals(ByteAdapter.byteArrayToHexString(new byte[] {0, 1, 0, 4}), "00010004");
        assertEquals(ByteAdapter.byteArrayToHexString(new byte[] {12, 15, 0, 1, 7, 9, 11}), "0C0F000107090B");
        assertEquals(ByteAdapter.byteArrayToHexString(new byte[] {100, 31, 3, 47, 22, 9, 120}), "641F032F160978");
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
