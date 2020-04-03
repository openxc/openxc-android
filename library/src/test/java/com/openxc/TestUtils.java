package com.openxc;

import android.content.Context;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;

public class TestUtils {
    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
       // SystemClock.sleep(millis);

    }

    public static URI copyToStorage(Context context, int resource,
            String filename) {
        URI uri = null;
        try {
            uri = new URI("file:///sdcard/com.openxc/" + filename);
        } catch(URISyntaxException e) {
            fail("Couldn't construct resource URIs: " + e);
        }

        try {
            FileUtils.copyInputStreamToFile(
                    context.getResources().openRawResource(resource),
                        new File(uri));
        } catch(IOException e) {
            fail("Couldn't copy trace files to SD card" + e);
        }
        return uri;
    }
}
