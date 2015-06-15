package com.openxc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;

import android.content.Context;

public class TestUtils {
    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
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
