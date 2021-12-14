package com.tfip2021;

/*
Rewritten classes of standard Java library to implement shorter
polling times between reads from InputStream and writes to
OutputStream
*/

import java.io.IOException;
import java.io.PipedOutputStream;

public class FastPipedOutputStream extends PipedOutputStream {
    // Members copied from PipedOutputStream
    private volatile FastPipedInputStream sink;
    // Members copied from PipedOutputStream

    public synchronized void connect(FastPipedInputStream snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        } else if (sink != null || snk.connected) {
            throw new IOException("Already connected");
        }
        sink = snk;
        snk.in = -1;
        snk.out = 0;
        snk.connected = true;
    }

    @Override
    public void write(int b)  throws IOException {
        FastPipedInputStream sink = this.sink;
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
        sink.receive(b);
    }
}
