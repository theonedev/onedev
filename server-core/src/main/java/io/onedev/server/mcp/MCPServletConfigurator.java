package io.onedev.server.mcp;

import io.onedev.server.jetty.ServletConfigurator;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Servlet configurator for the MCP (Model Context Protocol) server.
 * 
 * This configurator registers the MCP servlet with the Jetty server
 * to handle MCP protocol requests at the /mcp endpoint.
 */
@Singleton
public class MCPServletConfigurator implements ServletConfigurator {
    
    private final MCPServerServlet mcpServerServlet;
    
    @Inject
    public MCPServletConfigurator(MCPServerServlet mcpServerServlet) {
        this.mcpServerServlet = mcpServerServlet;
    }
    
    @Override
    public void configure(ServletContextHandler context) {
        // Register the MCP servlet at the /mcp path
        ServletHolder mcpServletHolder = new ServletHolder(mcpServerServlet);
        context.addServlet(mcpServletHolder, "/mcp");
        
        // Also register at /mcp/* to handle all MCP-related requests
        context.addServlet(mcpServletHolder, "/mcp/*");
    }
} 