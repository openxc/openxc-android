package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

@RunWith(RobolectricTestRunner.class)
public class MessageKeyTest {
    MessageKey key;
    Map<String, Object> parts = new HashMap<>();

    @Before
    public void setup() {
        parts.put("foo", "bar");
        key = new MessageKey(parts);
    }

    @Test
    public void writeAndReadFromParcel() {
        Parcel parcel = Parcel.obtain();
        key.writeToParcel(parcel, 0);

        // Reset parcel for reading
        parcel.setDataPosition(0);

        MessageKey createdFromParcel =
                MessageKey.CREATOR.createFromParcel(parcel);
        assertThat(createdFromParcel, instanceOf(MessageKey.class));
        assertEquals(key, createdFromParcel);
    }
}
