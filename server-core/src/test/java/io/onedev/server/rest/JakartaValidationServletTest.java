package io.onedev.server.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.validation.ValidationFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.Test;

public class JakartaValidationServletTest {
    @Test
    public void validatesJakartaConstraintsThroughTheJakartaServletStack() throws Exception {
        var server = new Server();
        var connector = new LocalConnector(server);
        server.addConnector(connector);
        var context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new ServletContainer(
                new ResourceConfig(Resource.class, ValidationFeature.class, JacksonFeature.class))), "/*");
        try {
            server.start();
            assertTrue(connector.getResponse(request("/validation")).startsWith("HTTP/1.1 400"));
            assertTrue(connector.getResponse(request("/validation?name=x")).startsWith("HTTP/1.1 400"));
            assertTrue(connector.getResponse(request("/validation?name=valid")).startsWith("HTTP/1.1 200"));
            assertTrue(connector.getResponse(post("/validation/list", "[{}]")).startsWith("HTTP/1.1 400"));
            assertTrue(connector.getResponse(post("/validation/list", "[{\"name\":\"valid\"}]")).startsWith("HTTP/1.1 200"));
            assertTrue(connector.getResponse(post("/validation/map", "{\"key\":{}}")).startsWith("HTTP/1.1 400"));
            assertTrue(connector.getResponse(post("/validation/map", "{\"key\":{\"name\":\"valid\"}}")).startsWith("HTTP/1.1 200"));
        } finally {
            server.stop();
        }
    }

    private String request(String path) {
        return "GET " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
    }

    private String post(String path, String json) {
        return "POST " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n"
                + "Content-Type: application/json\r\nContent-Length: " + json.length() + "\r\n\r\n" + json;
    }

    @Path("/validation")
    public static class Resource {
        @GET
        public String get(@QueryParam("name") @NotNull @Size(min = 2) String name) {
            return name;
        }

        @POST
        @Path("/list")
        @Consumes("application/json")
        public String list(@NotNull List<@Valid Entry> entries) {
            return "valid";
        }

        @POST
        @Path("/map")
        @Consumes("application/json")
        public String map(@NotNull Map<String, @Valid Entry> entries) {
            return "valid";
        }
    }

    public static class Entry {
        private String name;

        @NotNull
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
