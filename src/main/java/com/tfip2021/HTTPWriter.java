package com.tfip2021;

import java.io.*;

public class HTTPWriter implements AutoCloseable {

    private final OutputStream out;

    public HTTPWriter(OutputStream out) {
        this.out = out;
    }

    public void flush() throws Exception {
        this.out.flush();
    }

    @Override
    public void close() {
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeString() throws Exception {
        writeString("");
    }

    public void writeString(String line) throws Exception {
        writeBytes("%s\r\n".formatted(line).getBytes("utf-8"));
    }

    public void writeBytes(byte[] buffer) throws Exception {
        writeBytes(buffer, 0, buffer.length);
    }

    public void writeBytes(byte[] buffer, int start, int offset) throws Exception {
        out.write(buffer, start, offset);
    }
}