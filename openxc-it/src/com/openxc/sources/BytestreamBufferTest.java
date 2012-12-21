package com.openxc.sources;

import java.util.List;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class BytestreamBufferTest extends AndroidTestCase {
    BytestreamBuffer buffer;

    @Override
    public void setUp() {
        buffer = new BytestreamBuffer();
    }

    @SmallTest
    public void testEmpty() {
        List<String> records = buffer.readLines();
        assertEquals(0, records.size());
    }

    @SmallTest
    public void testreadLinesOne() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);
        List<String> records = buffer.readLines();
        assertEquals(1, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        records = buffer.readLines();
        assertEquals(0, records.size());
    }

    @SmallTest
    public void testreadLinesTwo() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"miracle\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        List<String> records = buffer.readLines();
        assertEquals(2, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        assertTrue(records.get(1).indexOf("pork") != -1);
        assertTrue(records.get(1).indexOf("miracle") != -1);

        records = buffer.readLines();
        assertEquals(0, records.size());
    }

    @SmallTest
    public void testLeavePartial() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"mira").getBytes();
        buffer.receive(bytes, bytes.length);

        List<String> records = buffer.readLines();
        assertEquals(1, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        records = buffer.readLines();
        assertEquals(0, records.size());
    }

    @SmallTest
    public void testCompletePartial() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"mira").getBytes();
        buffer.receive(bytes, bytes.length);

        List<String> records = buffer.readLines();
        assertEquals("Should only have 1 complete record in the result",
                1, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        bytes = new String("cle\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        records = buffer.readLines();
        assertEquals(1, records.size());
        assertTrue(records.get(0).indexOf("pork") != -1);
        assertTrue(records.get(0).indexOf("miracle") != -1);

        records = buffer.readLines();
        assertEquals(0, records.size());
    }

}
