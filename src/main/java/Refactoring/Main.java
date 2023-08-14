package Refactoring;

import java.io.*;

public class Main {
    private final static int PORT = 9999;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.listen(PORT);
    }
}