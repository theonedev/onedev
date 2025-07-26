# OneDev MCP Server

This is an example Model Context Protocol (MCP) server implementation for OneDev. The MCP server provides a standardized way for AI assistants and other tools to interact with OneDev server data and functionality.

## Overview

The MCP server is implemented as a servlet that runs within the existing OneDev Jetty server. It handles HTTP requests and implements the MCP protocol to provide:

- **Resources**: Access to OneDev server information and configuration
- **Tools**: Operations that can be performed on the OneDev server

## Architecture

The MCP server consists of the following components:

1. **MCPServerServlet**: The main servlet that handles HTTP requests and implements the MCP protocol
2. **MCPServletConfigurator**: Configures the servlet with the Jetty server
3. **MCPModule**: Dependency injection configuration

## Endpoints

### GET /mcp
Returns an HTML page with documentation about the MCP server and available endpoints.

### POST /mcp
Main MCP protocol endpoint that handles JSON-RPC requests.

## Available Resources

- `server:info` - Basic server information (name, version, status)
- `server:config` - Server configuration (port, context path, session timeout)

## Available Tools

- `getServerStatus` - Get current server status including uptime and memory usage
- `createResource` - Create a new resource with a given URI and content

## Example Usage

### Initialize the MCP connection

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {}
}
```

### List available resources

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "resources/list",
  "params": {}
}
```

### Read a specific resource

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "resources/read",
  "params": {
    "uri": "server:info"
  }
}
```

### List available tools

```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "tools/list",
  "params": {}
}
```

### Call a tool

```json
{
  "jsonrpc": "2.0",
  "id": 5,
  "method": "tools/call",
  "params": {
    "name": "getServerStatus",
    "arguments": {}
  }
}
```

## Integration

The MCP server is automatically integrated into OneDev through the plugin system. The `MCPModule` extends `AbstractPluginModule` and is automatically discovered and loaded when OneDev starts.

The servlet is registered at the `/mcp` path and handles both GET requests (for documentation) and POST requests (for MCP protocol communication).

## Extending the MCP Server

To add new resources or tools:

1. **Add new resources**: Modify the `initializeExampleResources()` method in `MCPServerServlet`
2. **Add new tools**: Add new cases to the `handleCallTool()` method and implement the corresponding handler methods
3. **Add new MCP methods**: Add new cases to the `handleRequest()` method

## Protocol Compliance

This implementation follows the Model Context Protocol specification and supports:

- JSON-RPC 2.0 communication
- Resource listing and reading
- Tool listing and calling
- Proper error handling with JSON-RPC error responses

## Security Considerations

The current implementation is a basic example. In a production environment, you should consider:

- Authentication and authorization for MCP requests
- Rate limiting
- Input validation and sanitization
- Secure communication (HTTPS)
- Access control for sensitive server information 