package com.openxc.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
