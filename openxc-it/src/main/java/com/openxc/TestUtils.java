package com.openxc;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;

import android.content.Context;

import junit.framework.Assert;

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
            Assert.fail("Couldn't construct resource URIs: " + e);
        }

        try {
            FileUtils.copyInputStreamToFile(
                    context.getResources().openRawResource(resource),
                        new File(uri));
        } catch(IOException e) {
            Assert.fail("Couldn't copy trace files to SD card" + e);
        }
        return uri;
    }

}
