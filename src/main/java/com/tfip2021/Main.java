package com.tfip2021;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final int DEFAULT_PORT = 3000;
    private static final String[] DEFAULT_PATH = { "static" };

    public static void main(String[] args) throws IOException {
        if (args.length % 2 != 0)
            throw new IllegalArgumentException("Incorrect number of arguments");
        Integer portNumber = DEFAULT_PORT;
        String[] docRoot = DEFAULT_PATH;

        // Get and validate command line options
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                    portNumber = Integer.parseInt(args[i+1]);
                    break;
                case "--docRoot":
                    docRoot = args[i+1].split(":");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option given");
            }
            i++;
        }

        // Validate doc roots
        for (int i = 0; i < docRoot.length; i++) {
            File dir = new File(docRoot[i]);
            if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
                System.out.println(
                    "Folder at " + docRoot[i] + " cannot be found, accessed or " +
                    "is not a folder. Please try again with the correct docRoot."
                );
                System.exit(1);
            }
        }

        HTTPServer s = new HTTPServer(portNumber, docRoot);
        s.interfaceWithUser();
    }
}
