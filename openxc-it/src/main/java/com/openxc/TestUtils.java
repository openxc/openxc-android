package com.openxc;

public class TestUtils {
    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}
