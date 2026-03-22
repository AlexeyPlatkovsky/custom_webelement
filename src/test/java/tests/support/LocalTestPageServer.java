package tests.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class LocalTestPageServer {
    private static final String COMPONENT_PLAYGROUND_PATH = "/component-playground.html";
    private static HttpServer server;
    private static String baseUrl;

    private LocalTestPageServer() {
    }

    public static synchronized String ensureStarted() {
        if (server != null) {
            return baseUrl;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            server.createContext(COMPONENT_PLAYGROUND_PATH,
                    exchange -> writeHtml(exchange, "ui/component-playground.html"));
            server.start();
            baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            return baseUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to start local test page server", e);
        }
    }

    public static synchronized void stop() {
        if (server == null) {
            return;
        }

        server.stop(0);
        server = null;
        baseUrl = null;
    }

    private static void writeHtml(HttpExchange exchange, String resourcePath) throws IOException {
        byte[] body = loadResource(resourcePath);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        } finally {
            exchange.close();
        }
    }

    private static byte[] loadResource(String resourcePath) {
        try (InputStream stream = LocalTestPageServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test resource " + resourcePath, e);
        }
    }
}
