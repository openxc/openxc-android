package com.openxc.util;

import java.io.IOException;
import java.io.BufferedWriter;

public interface FileOpener {
    public BufferedWriter openForWriting(String path) throws IOException;
}
