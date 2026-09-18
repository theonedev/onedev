package io.onedev.server.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.server.ServerConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ProductUriComplianceTest {
    @TempDir
    Path installDir;

    @Test
    void scopedPackagesRetainRawPathWithoutAllowingOtherAmbiguousEncodings() throws Exception {
        Files.createDirectories(installDir.resolve("conf"));
        Files.writeString(installDir.resolve("conf/server.properties"),
                "http_host=0.0.0.0\nhttp_port=0\ncluster_ip=127.0.0.1\n");
        var server = new Server();
        new ProductConfigurator(new ServerConfig(installDir.toFile())).configure(server);
        var configuredConnector = (ServerConnector) server.getConnectors()[0];
        var configuration = configuredConnector.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration();
        server.setConnectors(new Connector[0]);
        var connector = new LocalConnector(server, new HttpConnectionFactory(configuration));
        server.addConnector(connector);
        var context = new ServletContextHandler();
        context.getServletHandler().setDecodeAmbiguousURIs(true);
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
                // Security filters access decoded servlet paths; package routing uses the raw URI.
                request.getServletPath();
                request.getPathInfo();
                response.getWriter().write(request.getRequestURI());
            }
        }), "/*");
        server.setHandler(context);
        try {
            server.start();
            for (String slash : new String[] {"%2f", "%2F"}) {
                String path = "/tutorial-packages/~npm/@tutorial" + slash + "local-check";
                var response = HttpTester.parseResponse(connector.getResponse(
                        "PUT " + path + " HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"));
                assertEquals(200, response.getStatus());
                assertEquals(path, response.getContent());
            }
            for (String path : new String[] {"/a/%2e%2e/private", "/a/%252fprivate", "/a/%00private"}) {
                var response = HttpTester.parseResponse(connector.getResponse(
                        "GET " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"));
                assertEquals(400, response.getStatus(), path);
            }
        } finally {
            server.stop();
        }
    }
}
