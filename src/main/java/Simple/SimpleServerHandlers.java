package Simple;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServerHandlers {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private final Map<String, Map<String, SimpleHandler>> handlers = new HashMap<>();

    public void listen(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Refactoring.Server started");
            while (true) {
                Socket socket = serverSocket.accept();
                runCon(socket);
            }
        }
    }

    public void runCon(Socket socket) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                        String requestLine;
                        requestLine = in.readLine();
                        SimpleRequest request = SimpleRequest.createRequest(requestLine);

                        String path = request.getPath();

                        if (!validPaths.contains(path)) {
                            if (!tryHandle(request, out)) {
                                out.write((
                                        "HTTP/1.1 404 Not Found\r\n" +
                                                "Content-Length: 0\r\n" +
                                                "Connection: close\r\n" +
                                                "\r\n"
                                ).getBytes());
                            }
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
                            String template = null;

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

    private boolean tryHandle(SimpleRequest request, BufferedOutputStream out) throws IOException {
        boolean result = false;
        if (handlers.containsKey(request.getMethod())) {
            Map<String, SimpleHandler> stringHandlerMap = handlers.get(request.getMethod());
            if (stringHandlerMap.containsKey(request.getPath())) {
                SimpleHandler handler = stringHandlerMap.get(request.getPath());
                handler.handle(request, out);
                result = true;
            }
        }
        return result;
    }

    public void addHandler(String method, String path, SimpleHandler handler) {
        if (handlers.containsKey(method)) {
            Map<String, SimpleHandler> stringHandlerMap = handlers.get(method);
            stringHandlerMap.put(path, handler);
        } else {
            Map<String, SimpleHandler> stringHandler = new HashMap<>();
            stringHandler.put(path, handler);
            handlers.put(method, stringHandler);
        }
    }
}