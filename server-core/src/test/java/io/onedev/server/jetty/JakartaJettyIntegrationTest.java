package io.onedev.server.jetty;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.ws.api.ServletRequestCopy;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServerContainer;
import org.eclipse.jetty.ee11.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.JettyWebSocketFrameHandlerFactory;
import org.eclipse.jetty.websocket.core.WebSocketComponents;
import org.junit.jupiter.api.Test;

import io.onedev.agent.AgentSocket;
import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.server.agent.ServerSocket;
import io.onedev.server.web.websocket.WebSocketProcessor;

public class JakartaJettyIntegrationTest {
    @Test
    public void compressionPreservesMethodsContentTypesAndRequestBodies() throws Exception {
        var server = new Server();
        var connector = new LocalConnector(server);
        server.addConnector(connector);
        var context = new ServletContextHandler();
        context.setContextPath("/");
        String content = "compressible content ".repeat(100);
        byte[] original = content.getBytes(StandardCharsets.UTF_8);
        var compressed = new java.io.ByteArrayOutputStream();
        try (var gzip = new java.util.zip.GZIPOutputStream(compressed)) {
            gzip.write(original);
        }
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
                response.setContentType(request.getRequestURI().equals("/binary") ? "application/octet-stream" : "text/plain");
                byte[] body = request.getRequestURI().equals("/echo") ? request.getInputStream().readAllBytes() : original;
                response.setContentLength(body.length);
                response.getOutputStream().write(body);
            }
        }), "/*");
        server.setHandler(DefaultJettyService.newCompressionHandler(context));
        try {
            server.start();
            for (String method : new String[] {"GET", "POST", "PUT"}) {
                var response = org.eclipse.jetty.http.HttpTester.parseResponse(connector.getResponse(
                        method + " /text HTTP/1.1\r\nHost: localhost\r\nAccept-Encoding: gzip\r\nConnection: close\r\n\r\n"));
                assertEquals(200, response.getStatus());
                assertEquals("gzip", response.get("Content-Encoding"));
                try (var gzip = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(response.getContentBytes()))) {
                    assertArrayEquals(original, gzip.readAllBytes());
                }
            }
            for (String path : new String[] {"/binary", "/text"}) {
                String method = path.equals("/binary") ? "GET" : "DELETE";
                var response = org.eclipse.jetty.http.HttpTester.parseResponse(connector.getResponse(
                        method + " " + path + " HTTP/1.1\r\nHost: localhost\r\nAccept-Encoding: gzip\r\nConnection: close\r\n\r\n"));
                assertNull(response.get("Content-Encoding"));
                assertArrayEquals(original, response.getContentBytes());
            }
            var request = org.eclipse.jetty.http.HttpTester.newRequest();
            request.setMethod("POST");
            request.setURI("/echo");
            request.put("Host", "localhost");
            request.put("Connection", "close");
            request.put("Content-Encoding", "gzip");
            request.setContent(compressed.toByteArray());
            var response = org.eclipse.jetty.http.HttpTester.parseResponse(connector.getResponse(request.generate()));
            assertArrayEquals(compressed.toByteArray(), response.getContentBytes());
        } finally {
            server.stop();
        }
    }

    @Test
    public void redirectsDoNotExposeTheBackendOrigin() throws Exception {
        var server = new Server();
        var connector = new LocalConnector(server);
        server.addConnector(connector);
        var context = new ServletContextHandler();
        context.setContextPath("/onedev");
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
                response.sendRedirect("../login?return=projects");
            }
        }), "/*");
        server.setHandler(context);
        try {
            server.start();
            String response = connector.getResponse(request("GET", "/onedev/projects/list"));
            assertTrue(response.startsWith("HTTP/1.1 302"));
            assertTrue(response.contains("Location: /onedev/login?return=projects\r\n"));
            assertFalse(response.contains("Location: http:"));
        } finally {
            server.stop();
        }
    }

    @Test
    public void websocketRequestCopyCanReadTheJakartaServletMetadata() throws Exception {
        var server = new Server();
        var connector = new LocalConnector(server);
        server.addConnector(connector);
        var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        var copy = new ServletRequestCopy[1];
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) {
                // WebSocketProcessor now receives this real servlet request during upgrade,
                // instead of the old Jetty adapter with unsupported metadata methods.
                copy[0] = new ServletRequestCopy(request);
                response.setStatus(200);
            }
        }), "/*");
        server.setHandler(context);
        try {
            server.start();
            String response = connector.getResponse("GET /socket HTTP/1.1\r\nHost: localhost\r\n"
                    + "Content-Type: text/plain;charset=UTF-8\r\nCookie: JSESSIONID=previous-session\r\n"
                    + "Connection: close\r\n\r\n");
            assertTrue(response.startsWith("HTTP/1.1 200"));
            assertEquals("UTF-8", copy[0].getCharacterEncoding());
            assertTrue("text/plain;charset=UTF-8".equalsIgnoreCase(copy[0].getContentType()));
            assertEquals("previous-session", copy[0].getRequestedSessionId());
        } finally {
            server.stop();
        }
    }

    @Test
    public void healthAndReadinessUseJettysHandlerContract() throws Exception {
        var server = new Server();
        var connector = new LocalConnector(server);
        server.addConnector(connector);
        var ready = new java.util.concurrent.atomic.AtomicBoolean();
        var stage = new java.util.concurrent.atomic.AtomicReference<>("Starting cluster");
        server.setHandler(new ProbeHandler(ready::get, stage::get));
        try {
            server.start();
            assertTrue(connector.getResponse(request("GET", "/healthz")).contains("200 OK"));
            for (String message : new String[] {"Starting cluster", "Initializing database", "Starting services"}) {
                stage.set(message);
                var response = org.eclipse.jetty.http.HttpTester.parseResponse(
                        connector.getResponse(request("GET", "/readyz")));
                assertEquals(503, response.getStatus());
                assertEquals(message + "\n", response.getContent());
                assertEquals("no-store", response.get("Cache-Control"));
            }
            String notReadyHead = connector.getResponse(request("HEAD", "/readyz"));
            assertTrue(notReadyHead.contains("503 Service Unavailable"));
            assertEquals("", notReadyHead.substring(notReadyHead.indexOf("\r\n\r\n") + 4));
            ready.set(true);
            var response = org.eclipse.jetty.http.HttpTester.parseResponse(
                    connector.getResponse(request("GET", "/readyz")));
            assertEquals(200, response.getStatus());
            assertEquals("ok\n", response.getContent());
            String head = connector.getResponse(request("HEAD", "/healthz"));
            assertFalse(head.substring(head.indexOf("\r\n\r\n") + 4).contains("ok"));
            String post = connector.getResponse(request("POST", "/healthz"));
            assertTrue(post.contains("405 Method Not Allowed"));
            assertTrue(post.contains("Allow: GET, HEAD"));
            assertTrue(connector.getResponse(request("GET", "/unhandled")).contains("404 Not Found"));
        } finally {
            server.stop();
        }
    }

    @Test
    public void agentAndWicketEndpointsHaveValidMetadataAndBinaryMessagesRoundTrip() throws Exception {
        var server = new Server();
        var connector = new ServerConnector(server);
        connector.setHost("127.0.0.1");
        connector.setPort(0);
        server.addConnector(connector);
        var context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        JettyWebSocketServletContainerInitializer.configure(context, null);
        context.addServlet(new ServletHolder(new JettyWebSocketServlet() {
            @Override
            protected void configure(JettyWebSocketServletFactory factory) {
                factory.setCreator((request, response) -> new Echo());
            }
        }), "/socket");
        try (var client = new WebSocketClient()) {
            server.start();
            var factory = new JettyWebSocketFrameHandlerFactory(
                    JettyWebSocketServerContainer.getContainer(context.getServletContext()), new WebSocketComponents());
            assertNotNull(factory.getMetadata(AgentSocket.class));
            assertNotNull(factory.getMetadata(ServerSocket.class));
            assertNotNull(factory.getMetadata(WebSocketProcessor.class));
            client.start();
            var messages = new LinkedBlockingQueue<String>();
            var listener = new ReceivingListener(messages);
            Session session = client.connect(listener, URI.create("ws://127.0.0.1:" + connector.getLocalPort() + "/socket"))
                    .get(10, TimeUnit.SECONDS);
            for (int i = 0; i < 100; i++)
                new Message(MessageTypes.HEART_BEAT, "message-" + i).sendBy(session);
            for (int i = 0; i < 100; i++)
                assertEquals("message-" + i, messages.poll(10, TimeUnit.SECONDS));
            session.close();
        } finally {
            server.stop();
        }
    }

    public static class ReceivingListener implements Session.Listener.AutoDemanding {
        private final LinkedBlockingQueue<String> messages;

        public ReceivingListener(LinkedBlockingQueue<String> messages) {
            this.messages = messages;
        }

        @Override
        public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
            byte[] bytes = new byte[payload.remaining()];
            payload.get(bytes);
            messages.add(new String(Message.of(bytes, 0, bytes.length).getData(), StandardCharsets.UTF_8));
            callback.succeed();
        }
    }

    public static class Echo implements Session.Listener.AutoDemanding {
        private Session session;

        @Override
        public void onWebSocketOpen(Session session) {
            this.session = session;
        }

        @Override
        public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
            session.sendBinary(payload, callback);
        }
    }

    private static String request(String method, String path) {
        return method + " " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
    }
}
