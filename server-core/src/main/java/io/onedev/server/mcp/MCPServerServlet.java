package io.onedev.server.mcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.onedev.server.util.IOUtils;

/**
 * Example MCP (Model Context Protocol) Server Servlet
 * 
 * This servlet implements a basic MCP server that can handle:
 * - List resources
 * - Read resources  
 * - List tools
 * - Call tools
 * 
 * The MCP server provides access to OneDev server information and basic operations.
 */
@Singleton 
public class MCPServerServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPServerServlet.class);
    
    private final ObjectMapper objectMapper;
    
    // In-memory storage for demo purposes
    private final Map<String, JsonNode> resources = new ConcurrentHashMap<>();
    
    @Inject
    public MCPServerServlet(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Initialize with some example resources
        initializeExampleResources();
    }
    
    private void initializeExampleResources() {
        try {
            ObjectNode serverInfo = objectMapper.createObjectNode();
            serverInfo.put("name", "OneDev Server");
            serverInfo.put("version", "1.0.0");
            serverInfo.put("status", "running");
            resources.put("server:info", serverInfo);
            
            ObjectNode config = objectMapper.createObjectNode();
            config.put("port", 8080);
            config.put("contextPath", "/");
            config.put("sessionTimeout", 300);
            resources.put("server:config", config);
            
        } catch (Exception e) {
            logger.error("Error initializing example resources", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            var os = new ByteArrayOutputStream();
            try (var is = request.getInputStream()) {
                IOUtils.copy(is, os, IOUtils.BUFFER_SIZE);
            }                    

            System.out.println(os.toString(StandardCharsets.UTF_8));
                        
            JsonNode requestNode = objectMapper.readTree(os.toString(StandardCharsets.UTF_8));
            String method = requestNode.get("method").asText();
            JsonNode params = requestNode.get("params");
            Long id = requestNode.has("id")?requestNode.get("id").asLong():null;
            
            JsonNode result = handleRequest(method, params);
            
            // Send response
            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.put("jsonrpc", "2.0");
            
            if (id != null) 
                responseNode.put("id", id);
            responseNode.set("result", result);
            
            System.out.println(objectMapper.writeValueAsString(responseNode));
            
            try (PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(responseNode));
            }
            
        } catch (Exception e) {
            logger.error("Error handling MCP request", e);
            sendErrorResponse(response, -1, "Internal server error: " + e.getMessage());
        }
    }
    
    private JsonNode handleRequest(String method, JsonNode params) {
        switch (method) {
            case "initialize":
                return handleInitialize(params);
            case "notifications/initialized":
                return handleNotificationInitialized(params);
            case "resources/list":
                return handleListResources(params);
            case "resources/read":
                return handleReadResource(params);
            case "tools/list":
                return handleListTools(params);
            case "tools/call":
                return handleCallTool(params);
            default:
                throw new IllegalArgumentException("Unknown method: " + method);
        }
    }
    
    private JsonNode handleInitialize(JsonNode params) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");
        
        // Declare server capabilities
        ObjectNode capabilities = objectMapper.createObjectNode();
        capabilities.set("tools", objectMapper.createObjectNode());
        capabilities.set("resources", objectMapper.createObjectNode());
        result.set("capabilities", capabilities);
        
        result.set("serverInfo", objectMapper.createObjectNode()
                .put("name", "OneDev MCP Server")
                .put("version", "1.0.0"));
        return result;
    }
    
    private JsonNode handleNotificationInitialized(JsonNode params) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("status", "initialized");
        return result;
    }
    
    private JsonNode handleListResources(JsonNode params) {
        ArrayNode resourcesArray = objectMapper.createArrayNode();
        
        for (String uri : resources.keySet()) {
            ObjectNode resource = objectMapper.createObjectNode();
            resource.put("uri", uri);
            resource.put("name", uri.substring(uri.lastIndexOf(':') + 1));
            resource.put("description", "OneDev server " + uri.substring(uri.lastIndexOf(':') + 1));
            resource.put("mimeType", "application/json");
            resourcesArray.add(resource);
        }
        
        ObjectNode result = objectMapper.createObjectNode();
        result.set("resources", resourcesArray);
        return result;
    }
    
    private JsonNode handleReadResource(JsonNode params) {
        String uri = params.get("uri").asText();
        JsonNode resource = resources.get(uri);
        
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + uri);
        }
        
        ObjectNode result = objectMapper.createObjectNode();
        result.set("contents", objectMapper.createArrayNode().add(objectMapper.createObjectNode()
                .put("uri", uri)
                .put("mimeType", "application/json")
                .set("text", resource)));
        return result;
    }
    
    private JsonNode handleListTools(JsonNode params) {
        ArrayNode toolsArray = objectMapper.createArrayNode();
        
        // Example tools
        ObjectNode serverStatusTool = objectMapper.createObjectNode();
        serverStatusTool.put("name", "getServerStatus");
        serverStatusTool.put("description", "Get the current status of the OneDev server");
        
        // Input schema for getServerStatus 
        ObjectNode serverStatusInputSchema = objectMapper.createObjectNode();
        serverStatusInputSchema.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        serverStatusInputSchema.set("properties", properties);
        serverStatusTool.set("inputSchema", serverStatusInputSchema);
        
        toolsArray.add(serverStatusTool);
        
        ObjectNode createResourceTool = objectMapper.createObjectNode();
        createResourceTool.put("name", "createResource");
        createResourceTool.put("description", "Create a new resource");
        
        // Input schema for createResource
        ObjectNode createResourceInputSchema = objectMapper.createObjectNode();
        createResourceInputSchema.put("type", "object");
        properties = objectMapper.createObjectNode();
        properties.set("uri", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "URI identifier for the new resource"));
        properties.set("content", objectMapper.createObjectNode()
                .put("type", "object")
                .put("description", "Content object to store in the resource"));
        createResourceInputSchema.set("properties", properties);
        createResourceInputSchema.set("required", objectMapper.createArrayNode().add("uri").add("content"));
        createResourceTool.set("inputSchema", createResourceInputSchema);
                        
        toolsArray.add(createResourceTool);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.set("tools", toolsArray);
        return result;
    }
    
    private JsonNode handleCallTool(JsonNode params) {
        String name = params.get("name").asText();
        JsonNode arguments = params.get("arguments");

        JsonNode toolResult;
        switch (name) {
            case "getServerStatus":
                toolResult = handleGetServerStatus(arguments);
                break;
            case "createResource":
                toolResult = handleCreateResource(arguments);
                break;
            default:
                throw new IllegalArgumentException("Unknown tool: " + name);
        }
        
        // Format the response according to MCP specification
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode contentArray = objectMapper.createArrayNode();
        
        ObjectNode contentItem = objectMapper.createObjectNode();
        contentItem.put("type", "text");
        contentItem.put("text", toolResult.toString());
        contentArray.add(contentItem);
        
        result.set("content", contentArray);
        return result;
    }

    private JsonNode handleGetServerStatus(JsonNode arguments) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("status", "running");
        return result;
    }
    
    private JsonNode handleCreateResource(JsonNode arguments) {
        String uri = arguments.get("uri").asText();
        JsonNode content = arguments.get("content");
        
        if (resources.containsKey(uri)) {
            throw new IllegalArgumentException("Resource already exists: " + uri);
        }
        
        resources.put(uri, content);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("uri", uri);
        result.put("created", true);
        return result;
    }
    
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("jsonrpc", "2.0");
        errorNode.put("id", (String)null);
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        errorNode.set("error", error);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(errorNode));
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/plain");
        try (var os = response.getOutputStream()) {
            os.println("OneDev MCP server");
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
} 