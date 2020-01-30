package com.openxc.messages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MultiFrameResponseTest {
    MultiFrameResponse message = new MultiFrameResponse();
    int frame = 1;
    int messageId = 123;
    String payload = "{\"timestamp\":88,\"frame\":1,\"message_id\":345,\"bus\":1,\"total_size\":200,\"pid\":34,\"value\":93,\"payload\":\"{\\\"timestamp\\\":0,\\\"name\\\":\\\"torque_at_transmission\\\",\\\"value\\\":32.5}\"}";
    int totalSize = payload.length();

    @Before
    public void setup() {
        message = new MultiFrameResponse(frame, totalSize, messageId, totalSize, payload);
        message.clear();
    }

    @Test
    public void getFrameReturnsFrame() {
        assertEquals(frame, message.getFrame());
    }

    @Test
    public void getMessageIdReturnsMessageId() {
        assertEquals(messageId, message.getMessageId());
    }

    @Test
    public void getTotalSizeReturnsTotalSize() {
        assertEquals(totalSize, message.getTotalSize());
    }

    @Test
    public void getSizeReturnsSize() {
        assertEquals(totalSize, message.getSize());
    }

    @Test
    public void getPayloadReturnsPayload() {
        assertEquals(payload, message.getPayload());
    }

    @Test
    public void combineTwoFramesReturnsCombinedMessage() {
        String payload1 = "{\"timestamp\":88,\"frame\":1,\"message_id\":345,\"bus\":1,\"total";
        String payload2 = "_size\":200,\"pid\":34,\"value\":93,\"payload\":\"{\\\"timestamp\\\":0,\\\"name\\\":\\\"torque_at_transmission\\\",\\\"value\\\":32.5}\"}";

        int tSize = payload1.length() + payload2.length();
        MultiFrameResponse message1 = new MultiFrameResponse(frame, tSize, messageId, payload1.length(), payload1);
        boolean addResult1 = message1.addSequentialData();
        MultiFrameResponse message2  = new MultiFrameResponse(frame, tSize, messageId, payload2.length(), payload2);
        boolean addResult2 = message2.addSequentialData();

        String result = message2.getAssembledMessage();
        String expected = payload1 + payload2;
        assertEquals(expected, result);
        assertEquals(false, addResult1);
        assertEquals(true, addResult2);
    }

    @Test
    public void combineTwoFramesDiffMessageReturnsSeparateMessage() {
        String payload1 = "{\"timestamp\":88,\"frame\":1,\"message_id\":345,\"bus\":1,\"total";
        String payload2 = "_size\":200,\"pid\":34,\"value\":93,\"payload\":\"{\\\"timestamp\\\":0,\\\"name\\\":\\\"torque_at_transmission\\\",\\\"value\\\":32.5}\"}";

        int tSize = payload1.length() + payload2.length();
        MultiFrameResponse message1 = new MultiFrameResponse(frame, tSize, messageId, payload1.length(), payload1);
        boolean addResult1 = message1.addSequentialData();
        String result1 = message1.getAssembledMessage();
        assertEquals(payload1, result1);

        MultiFrameResponse message2  = new MultiFrameResponse(frame, tSize, messageId+1, payload2.length(), payload2);
        boolean addResult2 = message2.addSequentialData();
        String result2 = message2.getAssembledMessage();

        assertEquals(payload2, result2);
        assertEquals(false, addResult1);
        assertEquals(false, addResult2);
    }

    @Test
    public void combineTwoFramesShortLenReturnsFalseForComplete() {
        String payload1 = "{\"timestamp\":88,\"frame\":1,\"message_id\":345,\"bus\":1,\"total";
        String payload2 = "_size\":200,\"pid\":34,\"value\":93,\"payload\":\"{\\\"timestamp\\\":0,\\\"name\\\":\\\"torque_at_transmission\\\",\\\"value\\\":32.5}\"}";

        int tSize = payload1.length() + payload2.length();
        MultiFrameResponse message1 = new MultiFrameResponse(frame, tSize+1, messageId, payload1.length(), payload1);
        boolean addResult1 = message1.addSequentialData();
        String result1 = message1.getAssembledMessage();

        MultiFrameResponse message2  = new MultiFrameResponse(frame, tSize+1, messageId+1, payload2.length(), payload2);
        boolean addResult2 = message2.addSequentialData();
        String result2 = message2.getAssembledMessage();

        assertEquals(payload1, result1);
        assertEquals(payload2, result2);
        assertEquals(false, addResult1);
        assertEquals(false, addResult2);
    }

    @Test
    public void combineThreeFramesReturnsCombinedMessage() {
        String payload1 = "{\"timestamp\":88,\"frame\":1,\"message_id\":345,\"bus\":1,\"total";
        String payload2 = "_size\":200,\"pid\":34,\"value\":93,\"payload\":\"{\\\"timestamp\\\":";
        String payload3 = "0,\\\"name\\\":\\\"torque_at_transmission\\\",\\\"value\\\":32.5}\"}";

        int tSize = payload1.length() + payload2.length() + payload3.length();
        MultiFrameResponse message1 = new MultiFrameResponse(frame, tSize, messageId, payload1.length(), payload1);
        boolean addResult1 = message1.addSequentialData();
        MultiFrameResponse message2  = new MultiFrameResponse(frame, tSize, messageId, payload2.length(), payload2);
        boolean addResult2 = message2.addSequentialData();
        MultiFrameResponse message3  = new MultiFrameResponse(frame, tSize, messageId, payload3.length(), payload3);
        boolean addResult3 = message3.addSequentialData();

        String result = message3.getAssembledMessage();
        String expected = payload1 + payload2 + payload3;
        assertEquals(expected, result);
        assertEquals(false, addResult1);
        assertEquals(false, addResult2);
        assertEquals(true, addResult3);
    }

}
