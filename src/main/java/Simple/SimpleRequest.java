package Simple;


import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleRequest {

    private String method;
    private String path;
    private String body;
    private static List<NameValuePair> params;
    public static final String GET = "GET";
    public static final String POST = "POST";

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public SimpleRequest(String method, String path, String body, List<NameValuePair> params) {
        this.path = path;
        this.method = method;
        this.body = body;
        this.params = params;
    }

    public static SimpleRequest createRequest(BufferedInputStream in, int limit) throws IOException {
        final var allowedMethods = List.of(GET, POST);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            throw new IOException("Bad Request");
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            throw new IOException("Bad Request");
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            throw new IOException("Bad Request");
        }
        System.out.println(method);

        String path = null;

        //params = URLEncodedUtils.parse(requestLine[1], StandardCharsets.UTF_8,'&');
        try {
            URI uri = new URI(requestLine[1]);
            params = URLEncodedUtils.parse(uri.getQuery(), StandardCharsets.UTF_8);
            path = uri.getPath();
        } catch (URISyntaxException e) {
            e.getMessage();
        }
        if (!path.startsWith("/")) {
            throw new IOException("Bad Request");
        }

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            throw new IOException("Bad Request");
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        System.out.println(headers);
        String body = null;
        // для GET тела нет
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                body = new String(bodyBytes);
                params.addAll(URLEncodedUtils.parse(body,StandardCharsets.UTF_8));
                System.out.println(body);
            }
        }
        return new SimpleRequest(method, path, body, params);
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }


    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static Optional<NameValuePair> getQueryParam(String parameter) {
        return params.stream().filter(x -> x.getName().equals(parameter)).findFirst();
    }

    public static List<NameValuePair> getQueryParams() {
        return params;
    }

    public static Map<String, List<String>> getPostParam(String name) {
        Map<String, List<String>> queryParams = new HashMap<>();
        for (NameValuePair pair : params) {
            if (pair.getName().equals(name)) {
                if (!queryParams.containsKey(pair.getName())) {
                    queryParams.put(pair.getName(), new ArrayList<>());
                }
                queryParams.get(pair.getName()).add(pair.getValue());
            }
        }
        return queryParams;
    }

    public static Map<String, List<String>> getPostParams() {
        Map<String, List<String>> queryParams = new HashMap<>();
        for (NameValuePair pair : params) {
            if (!queryParams.containsKey(pair.getName())) {
                queryParams.put(pair.getName(), new ArrayList<>());
            }
            queryParams.get(pair.getName()).add(pair.getValue());
        }
        return queryParams;
    }
}