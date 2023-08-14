package Refactoring;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public Thread thread;
    public Socket socket;
    public BufferedReader in;
    public BufferedOutputStream out;
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    public ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public void listen(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Refactoring.Server started");
            while (true) {
                socket = serverSocket.accept();
                runCon();
            }
        }
    }

    public void runCon() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                while (true) {
                    try {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new BufferedOutputStream(socket.getOutputStream());
                        String requestLine;
                        requestLine = in.readLine();
                        final var parts = requestLine.split(" ");

                        if (parts.length != 3) {
                            // just close socket
                            continue;
                        }

                        final var path = parts[1];
                        if (!validPaths.contains(path)) {
                            out.write((
                                    "HTTP/1.1 404 Not Found\r\n" +
                                            "Content-Length: 0\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());
                            out.flush();
                            continue;
                        }

                        final var filePath = Path.of(".", "public", path);
                        String mimeType = null;
                        try {
                            mimeType = Files.probeContentType(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // special case for classic
                        if (path.equals("/classic.html")) {
                            String template;

                            template = Files.readString(filePath);
                            final var content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            out.write((
                                    "HTTP/1.1 200 OK\r\n" +
                                            "Content-Type: " + mimeType + "\r\n" +
                                            "Content-Length: " + content.length + "\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());

                            out.write(content);
                            out.flush();
                            continue;
                        }

                        final long length;
                        length = Files.size(filePath);
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, out);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        threadPool.submit(thread);
        if (!thread.isAlive())
            Thread.currentThread().interrupt();
    }
}