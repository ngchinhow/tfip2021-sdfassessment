package com.tfip2021;

/*
Rewritten classes of standard Java library to implement shorter
polling times between reads from InputStream and writes to
OutputStream
*/

import java.io.IOException;
import java.io.PipedInputStream;

public class FastPipedInputStream extends PipedInputStream {
    // Members copied from PipedInputStream
    boolean closedByWriter;
    volatile boolean closedByReader;
    boolean connected;
    Thread readSide;
    Thread writeSide;
    private static final int DEFAULT_PIPE_SIZE = 1024;
    protected static final int PIPE_SIZE = DEFAULT_PIPE_SIZE;
    protected byte buffer[];
    protected int in = -1;
    protected int out = 0;
    // Members copied from PipedInputStream

    public FastPipedInputStream(FastPipedOutputStream src, int pipeSize)
            throws IOException {
        initPipe(pipeSize);
        connect(src);
    }

    public FastPipedInputStream(FastPipedOutputStream pos) throws IOException {
        this(pos, DEFAULT_PIPE_SIZE);
    }

    public void connect(FastPipedOutputStream src) throws IOException {
        src.connect(this);
    }

    @Override
    public synchronized int read() throws IOException {
        if (!connected) {
            System.out.println("Failure in reading from fast pipe");
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive()
                && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }

        readSide = Thread.currentThread();
        int trials = 2;
        while (in < 0) {
            if (closedByWriter) {
                /* closed by writer, return EOF */
                return -1;
            }
            if ((writeSide != null) && (!writeSide.isAlive()) && (--trials < 0)) {
                throw new IOException("Pipe broken");
            }
            /* might be a writer waiting */
            notifyAll();
            try {
                wait(200);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        int ret = buffer[out++] & 0xFF;
        if (out >= buffer.length) {
            out = 0;
        }
        if (in == out) {
            /* now empty */
            in = -1;
        }

        return ret;
    }

    @Override
    protected synchronized void receive(int b) throws IOException {
        checkStateForReceive();
        writeSide = Thread.currentThread();
        if (in == out)
            awaitSpace();
        if (in < 0) {
            in = 0;
            out = 0;
        }
        buffer[in++] = (byte) (b & 0xFF);
        if (in >= buffer.length) {
            in = 0;
        }
    }

    private void initPipe(int pipeSize) {
        if (pipeSize <= 0) {
            throw new IllegalArgumentException("Pipe Size <= 0");
        }
        buffer = new byte[pipeSize];
    }

    private void checkStateForReceive() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("Read end dead");
        }
    }

    private void awaitSpace() throws IOException {
        while (in == out) {
            checkStateForReceive();

            /* full: kick any waiting readers */
            notifyAll();
            try {
                wait(200);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
    }
}
