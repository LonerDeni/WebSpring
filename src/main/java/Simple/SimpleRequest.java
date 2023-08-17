package Simple;


import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleRequest {

    private String method;
    private String path;
    private String body;
    private static List<NameValuePair> params;

    public SimpleRequest(String method, String path, String body, List<NameValuePair> params) {
        this.path = path;
        this.method = method;
        this.body = body;
        this.params = params;
    }

    public static SimpleRequest createRequest(String request) throws IOException {
        final var parts = request.lines().collect(Collectors.toList());

        if (parts.size() < 1) {
            System.out.println("Bad request");//400
            throw new IOException("Invalid request");
        }
        String[] requestLineParts = parts.get(0).split(" ");

        List<NameValuePair> params = new ArrayList<>();
        String path = null;
        try {
            URI uri = new URI(requestLineParts[1]);
            params = URLEncodedUtils.parse(new URI(requestLineParts[1]).getQuery(), Charset.forName("UTF-8"));
            path = uri.getPath();
        } catch (URISyntaxException e) {
            e.getMessage();
        }

        if (requestLineParts.length == 0) {
            System.out.println("Bad request");//400
            throw new IOException("Invalid request");
        }

        String method = requestLineParts[0];
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

        return new SimpleRequest(method, path, body, params);
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

    public static Optional<NameValuePair> getQueryParam(String parameter) {
        return params.stream().filter(x -> x.getName().equals(parameter)).findFirst();
    }

    public static List<NameValuePair> getQueryParams() {
        return params;
    }
}