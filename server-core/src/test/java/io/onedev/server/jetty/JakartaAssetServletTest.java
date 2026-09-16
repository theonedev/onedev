package io.onedev.server.jetty;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.server.assets.FileAssetServlet;

public class JakartaAssetServletTest {
    @TempDir
    public Path folder;

    @Test
    public void servesMappedAssetsWithMimeTypesRangesAndCachePolicy() throws Exception {
        Files.writeString(folder.resolve("module.mjs"), "export const value = 42;");
        Files.writeString(folder.resolve("prefetch.json"), "{}");
        Files.writeString(folder.resolve("space name.txt"), "encoded path");
        var server = new Server();
        var connector = new LocalConnector(server);
        server.addConnector(connector);
        var context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder("assets", new FileAssetServlet(folder.toFile().getCanonicalFile())), "/assets/*");
        context.addServlet(new ServletHolder("prefetch", new FileAssetServlet(folder.toFile().getCanonicalFile())), "/prefetch.json");
        try {
            server.start();
            String module = connector.getResponse(request("/assets/module.mjs", ""));
            assertTrue(module.startsWith("HTTP/1.1 200"), module);
            assertTrue(module.contains("Content-Type: text/javascript"), module);
            assertTrue(module.contains("Cache-Control:"), module);
            assertTrue(module.endsWith("export const value = 42;"), module);
            String range = connector.getResponse(request("/assets/module.mjs", "Range: bytes=0-5\r\n"));
            assertTrue(range.startsWith("HTTP/1.1 206"), range);
            assertTrue(range.endsWith("export"), range);
            String prefetch = connector.getResponse(request("/prefetch.json", ""));
            assertTrue(prefetch.contains("Content-Type: application/speculationrules+json"), prefetch);
            assertTrue(connector.getResponse(request("/assets/space%20name.txt", "")).endsWith("encoded path"));
            assertTrue(connector.getResponse(request("/assets/missing", "")).startsWith("HTTP/1.1 404"));
            assertTrue(connector.getResponse(request("/assets/", "")).startsWith("HTTP/1.1 403"));
        } finally {
            server.stop();
        }
    }

    private static String request(String path, String headers) {
        return "GET " + path + " HTTP/1.1\r\nHost: localhost\r\n" + headers + "Connection: close\r\n\r\n";
    }
}
