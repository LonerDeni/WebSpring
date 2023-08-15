package Simple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleRequest {

    private String method;
    private String path;
    private String body;

    public SimpleRequest(String method, String path, String body) {
        this.path = path;
        this.method = method;
        this.body = body;
    }

    public static SimpleRequest createRequest(String request) throws IOException {

        final var parts = request.lines().collect(Collectors.toList());

        if (parts.size() < 1) {
            System.out.println("Bad request");//400
            throw new IOException("Invalid request");
        }
        String[] requestLineParts = parts.get(0).split(" ");

        if (requestLineParts.length == 0) {
            System.out.println("Bad request");//400
            throw new IOException("Invalid request");
        }

        String method = requestLineParts[0];
        String path = requestLineParts[1];
        List<String> headers = new ArrayList<>();
        String body;

        int count = 1;
        StringBuilder builder = new StringBuilder();

        //add Body
        for (int i = count; parts.size() > i; i++) {
            if (i != count)
                builder.append("\n");
            builder.append(parts.get(i));
        }
        body = builder.toString();

        return new SimpleRequest(method, path, body);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }
}