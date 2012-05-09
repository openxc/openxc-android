package com.openxc.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A file opener that uses the standard Java IO API.
 *
 * This file opener will work in a regular JVM, so it is good for test cases.
 */
public class JavaFileOpener implements FileOpener {
    private final boolean APPEND = true;

    public BufferedWriter openForWriting(String path) throws IOException {
        try {
            return new BufferedWriter(new FileWriter(path, APPEND));
        } catch(IOException e) {
            throw e;
        }
    }
}
