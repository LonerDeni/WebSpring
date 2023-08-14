package Simple;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleMainHandlers {
    private final static int PORT = 9999;
    ;


    public static void main(String[] args) throws IOException {
        final var server = new SimpleServerHandlers();
        // код инициализации сервера (из вашего предыдущего ДЗ)
        // добавление хендлеров (обработчиков)

        server.addHandler("GET", "/messages", new SimpleHandler() {
            @Override
            public void handle(SimpleRequest request, BufferedOutputStream responseStream) {
                try {
                    final var filePath = Path.of(".", "public/spring.png");
                    long length = Files.size(filePath);
                    String mimeType = Files.probeContentType(filePath);
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, responseStream);
                    responseStream.flush();
                } catch (IOException e) {
                    e.getMessage();
                }
            }
        });
        server.addHandler("POST", "/messages", new SimpleHandler() {
            public void handle(SimpleRequest request, BufferedOutputStream responseStream) {
                try {
                    final var filePath = Path.of(".", "public/links.html");
                    long length = Files.size(filePath);
                    String mimeType = Files.probeContentType(filePath);
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, responseStream);
                    responseStream.flush();
                } catch (IOException e) {
                    e.getMessage();
                }
            }
        });
        server.listen(PORT);
    }
}