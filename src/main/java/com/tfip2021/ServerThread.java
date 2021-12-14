package com.tfip2021;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerThread implements Runnable {
    private ServerSocket serverSocket;
    private Socket socket;
    private String[] docRoot;
    private ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private ArrayList< HTTPClientConnection > clientThreads = new ArrayList< HTTPClientConnection >();

    public ServerSocket getServer() { return this.serverSocket; }
    public Socket getSocket() { return this.socket; }
    public String[] getDocRoot() { return this.docRoot; }
    public ExecutorService getThreadPool() { return this.threadPool; }
    public ArrayList< HTTPClientConnection > getClientThreads() { return this.clientThreads; }

    public void setSocket(Socket s) {
        this.socket = s;
    }
    public void setClientThread(HTTPClientConnection thread) {
        this.getClientThreads().add(thread);
    }

    public ServerThread(ServerSocket ss, String[] root) {
        this.serverSocket = ss;
        this.docRoot = root;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Waiting for new connection...");
                setSocket(this.getServer().accept());
                System.out.println("Got socket");
                HTTPClientConnection ch = new HTTPClientConnection(
                    this.getSocket(),
                    this.getDocRoot()
                );
                setClientThread(ch);
                System.out.println("Created thread");
                this.getThreadPool().submit(ch);
                System.out.println("Started thread");
            }
        } catch (SocketException e) { // ServerSocket closed by user
            System.out.println("Starting shutdown of server...");
            for (HTTPClientConnection t : this.getClientThreads()) {
                t.stop();
            }
            try {
                // Shutdown thread pool
                // Disable new tasks from being submitted
                this.getThreadPool().shutdown();
            
                // Wait a while for existing tasks to terminate
                if (!this.getThreadPool().awaitTermination(10, TimeUnit.SECONDS)) {
                    this.getThreadPool().shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!this.getThreadPool().awaitTermination(10, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                } else {
                    System.out.println("Done!");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                this.getThreadPool().shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
