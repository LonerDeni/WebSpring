package Simple;

import java.io.BufferedOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface SimpleHandler {
    void handle(SimpleRequest request, BufferedOutputStream out) throws IOException;
}
