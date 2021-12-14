package com.tfip2021;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.StringTokenizer;

public class HTTPClientConnection implements Runnable {
    private Socket socket;
    private String[] docRoot;
    
    public Socket getSocket() { return this.socket; }
    public String[] getDocRoot() { return this.docRoot; }

    protected void setSocket(Socket socket) {
        this.socket = socket;
    }

    public HTTPClientConnection (Socket socket, String[] root) {
        this.socket = socket;
        this.docRoot = root;
    }

    @Override
    public void run() {
        try (
            BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(this.getSocket().getInputStream())
            );
            HTTPWriter httpWriter = new HTTPWriter(this.getSocket().getOutputStream())
        ) {
            // Get request line
            String requestLine = inputReader.readLine();
            String header = requestLine;

            StringTokenizer tokenizer = new StringTokenizer(header);
            String method = tokenizer.nextToken();
            String query = tokenizer.nextToken();

            while (inputReader.ready()) {
                // Clear out request headers for next request
                inputReader.readLine();
            }

            if (!method.equals("GET")) {
                httpWriter.writeString(
                    "HTTP/1.1 405 Method Not Allowed\r\n\r\n" + 
                    method + " not supported\r\n"
                );
                return;
            }

            if (query.equals("/")) {
                // Replace empty resource with index.html
                query = "\\index.html";
            }
            int i = 0;
            boolean resourceExists = false;
            File resource = null;
            while (i < this.getDocRoot().length && !resourceExists) {
                resource = new File(this.getDocRoot()[i] + query);
                if (resource.exists() && resource.isFile()) {
                    resourceExists = true;
                }
                i++;
            }
            
            // Send resource if it exists; else 404 Not Found
            if (resourceExists) {
                sendResource(resource, httpWriter);
            } else {
                httpWriter.writeString(
                    "HTTP/1.1 404 Not Found\r\n\r\n" + 
                    query + " not found\r\n"
                );
            }
        } catch (SocketException e) {
            System.out.println("Socket has been closed!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendResource(File resource, HTTPWriter httpWriter) throws Exception {
        String mimeType = Files.probeContentType(resource.toPath());
        
        httpWriter.writeString(
            "HTTP/1.1 200 OK\r\n" + 
            "Content-Type: " + mimeType + "\r\n"
        );
        httpWriter.writeBytes(Files.readAllBytes(resource.toPath()));
    }

    public void stop() {
        try {
            this.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
