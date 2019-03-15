/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package webServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class javaWebsite {

    public static void main(String[] args) {
        try {
            int port = 80;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new StaticHandler("/", "calendars/webCalendars/"));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


class StaticHandler implements HttpHandler {

    private String routePath;
    private String fsPath;

    private Map<String, String> headers = new HashMap<>(){{
        put("html", "text/html");
        put("css", "text/css");
        put("js", "text/javascript");
        put("json", "application/json");
        put("svg", "image/svg+xml");
    }};

    StaticHandler(String path, String filesystemPath) throws FileNotFoundException {
        routePath = path;
        fsPath = filesystemPath;
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        OutputStream outputStream = http.getResponseBody();
        http.getRequestBody();
        String request = http.getRequestURI().getRawPath();
        byte[] result;
        int code;
        if(http.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                try {
                    String path = fsPath + request.substring(routePath.length());
                    System.out.println("requested: " + path);
                    result = read(new FileInputStream(path)).toByteArray();
                    String ext = request.substring(request.lastIndexOf(".") + 1);
                    if (headers.containsKey(ext))
                        http.getResponseHeaders().add("Content-Type", headers.get(ext));
                    code = 200;
                } catch (IOException e) {
                    result = (404 + " " + request).getBytes();
                    code = 404;
                }

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                result = sw.getBuffer().toString().getBytes();
                code = 500;
            }
            http.sendResponseHeaders(code, result.length);
            outputStream.write(result);
            outputStream.close();
        }
    }

    private static ByteArrayOutputStream read(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer;
    }
}