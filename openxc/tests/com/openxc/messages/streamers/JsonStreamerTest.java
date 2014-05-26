package com.openxc.messages.streamers;

import java.util.List;

import junit.framework.TestCase;

public class JsonStreamerTest extends TestCase {
    JsonStreamer streamer;

    @Override
    public void setUp() {
        streamer = new JsonStreamer();
    }

    public void testEmpty() {
        List<String> records = streamer.readLines();
        assertEquals(0, records.size());
    }

    public void testreadLinesOne() {
        byte[] bytes = new String("{\"key\": \"value\"}\u0000").getBytes();
        streamer.receive(bytes, bytes.length);
        List<String> records = streamer.readLines();
        assertEquals(1, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        records = streamer.readLines();
        assertEquals(0, records.size());
    }

    public void testreadLinesTwo() {
        byte[] bytes = new String("{\"key\": \"value\"}\u0000").getBytes();
        streamer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"miracle\"}\u0000").getBytes();
        streamer.receive(bytes, bytes.length);

        List<String> records = streamer.readLines();
        assertEquals(2, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        assertTrue(records.get(1).indexOf("pork") != -1);
        assertTrue(records.get(1).indexOf("miracle") != -1);

        records = streamer.readLines();
        assertEquals(0, records.size());
    }

    public void testLeavePartial() {
        byte[] bytes = new String("{\"key\": \"value\"}\u0000").getBytes();
        streamer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"mira").getBytes();
        streamer.receive(bytes, bytes.length);

        List<String> records = streamer.readLines();
        assertEquals(1, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        records = streamer.readLines();
        assertEquals(0, records.size());
    }

    public void testCompletePartial() {
        byte[] bytes = new String("{\"key\": \"value\"}\u0000").getBytes();
        streamer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"mira").getBytes();
        streamer.receive(bytes, bytes.length);

        List<String> records = streamer.readLines();
        assertEquals("Should only have 1 complete record in the result",
                1, records.size());
        assertTrue(records.get(0).indexOf("key") != -1);
        assertTrue(records.get(0).indexOf("value") != -1);

        bytes = new String("cle\"}\u0000").getBytes();
        streamer.receive(bytes, bytes.length);

        records = streamer.readLines();
        assertEquals(1, records.size());
        assertTrue(records.get(0).indexOf("pork") != -1);
        assertTrue(records.get(0).indexOf("miracle") != -1);

        records = streamer.readLines();
        assertEquals(0, records.size());
    }

}
