package com.openxc.util;

import java.io.IOException;
import java.io.BufferedWriter;

/**
 * A generic interface for opening a file for writing.
 *
 * There are multiple ways to open files for writing in Android, specifically
 * via an Android Context object or directly through the filesystem, and this interface
 * abstracts the details away depending on the situation.
 */
public interface FileOpener {
    /**
     * Open the file at the given path for writing.
     *
     * @return a BufferedWriter that references the requested file.
     * @throws IOException if there are issues opening the file
     */
    public BufferedWriter openForWriting(String path) throws IOException;
}
