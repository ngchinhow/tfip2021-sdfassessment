package com.tfip2021;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

public class HTTPServer {
    private ServerSocket server;
    private String[] docRoot;
    private Thread serverThread;
    private FastPipedOutputStream stagingPipe = new FastPipedOutputStream();
    private FastPipedInputStream releasingPipe = new FastPipedInputStream(stagingPipe);

    public ServerSocket getServer() { return this.server; }
    public Thread getServerThread() { return this.serverThread; }
    public FastPipedOutputStream getStagingPipe() { return this.stagingPipe; }
    public FastPipedInputStream getReleasingPipe() { return this.releasingPipe; }

    public HTTPServer(int port, String[] docRoot) throws IOException {
        this.docRoot = docRoot;
        this.server = new ServerSocket(port);
        this.serverThread = new Thread(new ServerThread(this.server, this.docRoot));
    }

    public void interfaceWithUser() throws IOException {
        Thread stagingThread = new Thread(
                new UserReader(this.getStagingPipe())
        );
        stagingThread.setDaemon(true);
        stagingThread.start();
        this.getServerThread().start();
        String operation = "";
        try (
            BufferedReader br = new BufferedReader(
                new InputStreamReader(this.getReleasingPipe())
            )
        ) {
            while (!operation.equals("close")) {
                operation = br.readLine().toLowerCase();
            }
        } finally {
            this.getServer().close();
        }
    }
}