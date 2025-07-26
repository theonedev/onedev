package io.onedev.server.mcp;

import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.server.jetty.ServletConfigurator;

/**
 * Module for the MCP (Model Context Protocol) server.
 * 
 * This module configures the dependency injection for MCP server components
 * and registers the servlet configurator with the Jetty server.
 */
public class MCPModule extends AbstractPluginModule {

    @Override
    protected void configure() {
        super.configure();
        
        // Bind the MCP server servlet as a singleton
        bind(MCPServerServlet.class).asEagerSingleton();
        
        // Contribute the MCP servlet configurator to the ServletConfigurator extension point
        contribute(ServletConfigurator.class, MCPServletConfigurator.class);
    }
} 