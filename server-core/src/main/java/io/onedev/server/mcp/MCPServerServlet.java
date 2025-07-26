package io.onedev.server.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.onedev.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class MCPServerServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPServerServlet.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // In-memory storage for demo purposes
    private final Map<String, JsonNode> resources = new ConcurrentHashMap<>();
    
    public MCPServerServlet() {
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
            // Read request body
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            
            JsonNode requestNode = objectMapper.readTree(requestBody.toString());
            String method = requestNode.get("method").asText();
            JsonNode params = requestNode.get("params");
            String id = requestNode.get("id").asText();
            
            JsonNode result = handleRequest(method, params);
            
            // Send response
            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.put("jsonrpc", "2.0");
            responseNode.put("id", id);
            responseNode.set("result", result);
            
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
        result.put("capabilities", objectMapper.createObjectNode());
        result.put("serverInfo", objectMapper.createObjectNode()
                .put("name", "OneDev MCP Server")
                .put("version", "1.0.0"));
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
        serverStatusTool.set("inputSchema", objectMapper.createObjectNode());
        toolsArray.add(serverStatusTool);
        
        ObjectNode createResourceTool = objectMapper.createObjectNode();
        createResourceTool.put("name", "createResource");
        createResourceTool.put("description", "Create a new resource");
        ObjectNode inputSchema = objectMapper.createObjectNode();
        inputSchema.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.put("uri", objectMapper.createObjectNode().put("type", "string"));
        properties.put("content", objectMapper.createObjectNode().put("type", "object"));
        inputSchema.set("properties", properties);
        inputSchema.set("required", objectMapper.createArrayNode().add("uri").add("content"));
        createResourceTool.set("inputSchema", inputSchema);
        toolsArray.add(createResourceTool);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.set("tools", toolsArray);
        return result;
    }
    
    private JsonNode handleCallTool(JsonNode params) {
        String name = params.get("name").asText();
        JsonNode arguments = params.get("arguments");
        
        switch (name) {
            case "getServerStatus":
                return handleGetServerStatus(arguments);
            case "createResource":
                return handleCreateResource(arguments);
            default:
                throw new IllegalArgumentException("Unknown tool: " + name);
        }
    }
    
    private JsonNode handleGetServerStatus(JsonNode arguments) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("status", "running");
        result.put("uptime", System.currentTimeMillis());
        result.put("memoryUsage", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        result.put("totalMemory", Runtime.getRuntime().totalMemory());
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
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>OneDev MCP Server</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 40px; }\n" +
                "        .container { max-width: 800px; margin: 0 auto; }\n" +
                "        .endpoint { background: #f5f5f5; padding: 20px; margin: 20px 0; border-radius: 5px; }\n" +
                "        .method { font-weight: bold; color: #0066cc; }\n" +
                "        .description { margin-top: 10px; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>OneDev MCP Server</h1>\n" +
                "        <p>This is an example Model Context Protocol (MCP) server for OneDev.</p>\n" +
                "        \n" +
                "        <div class=\"endpoint\">\n" +
                "            <div class=\"method\">POST /mcp</div>\n" +
                "            <div class=\"description\">\n" +
                "                Main MCP endpoint that handles all protocol requests including:\n" +
                "                <ul>\n" +
                "                    <li>initialize - Initialize the MCP connection</li>\n" +
                "                    <li>resources/list - List available resources</li>\n" +
                "                    <li>resources/read - Read a specific resource</li>\n" +
                "                    <li>tools/list - List available tools</li>\n" +
                "                    <li>tools/call - Call a specific tool</li>\n" +
                "                </ul>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <h2>Available Resources</h2>\n" +
                "        <ul>\n" +
                "            <li><code>server:info</code> - Basic server information</li>\n" +
                "            <li><code>server:config</code> - Server configuration</li>\n" +
                "        </ul>\n" +
                "        \n" +
                "        <h2>Available Tools</h2>\n" +
                "        <ul>\n" +
                "            <li><code>getServerStatus</code> - Get current server status</li>\n" +
                "            <li><code>createResource</code> - Create a new resource</li>\n" +
                "        </ul>\n" +
                "        \n" +
                "        <h2>Example Request</h2>\n" +
                "        <pre>\n" +
                "{\n" +
                "  \"jsonrpc\": \"2.0\",\n" +
                "  \"id\": 1,\n" +
                "  \"method\": \"resources/list\",\n" +
                "  \"params\": {}\n" +
                "}\n" +
                "        </pre>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>");
        }
    }
} 