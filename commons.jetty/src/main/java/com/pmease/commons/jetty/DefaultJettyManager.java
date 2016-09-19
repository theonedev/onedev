package com.pmease.commons.jetty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;
import com.pmease.commons.bootstrap.BootstrapUtils;

@Singleton
public class DefaultJettyManager implements JettyManager {
	
	private static final int MAX_CONTENT_SIZE = 5000000;
	
	private Server server;
	
	private ServletContextHandler contextHandler;
	
	private final Provider<Set<ServerConfigurator>> serverConfiguratorsProvider;
	
	private final Provider<Set<ServletConfigurator>> servletConfiguratorsProvider;
	
	/*
	 * Inject providers here to avoid circurlar dependencies when dependency graph gets complicated
	 */
	@Inject
	public DefaultJettyManager(
			Provider<Set<ServerConfigurator>> serverConfiguratorsProvider, 
			Provider<Set<ServletConfigurator>> servletConfiguratorsProvider) {
		this.serverConfiguratorsProvider = serverConfiguratorsProvider;
		this.servletConfiguratorsProvider = servletConfiguratorsProvider;
	}
	
	@Override
	public void start() {
		server = createServer();
		
		try {
			server.start();
		} catch (Exception e) {
			throw BootstrapUtils.unchecked(e);
		}
	}

	@Override
	public void stop() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				throw BootstrapUtils.unchecked(e);
			}
		}
	}
	
	@Override
	public ServletContextHandler getContextHandler() {
		return contextHandler;
	}
	
	private Server createServer() {
		server = new Server();

        contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setMaxFormContentSize(MAX_CONTENT_SIZE);

        contextHandler.setClassLoader(JettyPlugin.class.getClassLoader());
        
        contextHandler.setErrorHandler(new ErrorPageErrorHandler());
        contextHandler.addFilter(DisableTraceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        /*
         * By default contributions is in reverse dependency order. We reverse the order so that 
         * servlet and filter contributions in dependency plugins comes first. 
         */
        List<ServletConfigurator> servletConfigurators = new ArrayList<>(servletConfiguratorsProvider.get());
        Collections.reverse(servletConfigurators);
        for (ServletConfigurator configurator: servletConfigurators) {
        	configurator.configure(contextHandler);
        }

        /*
         *  Add Guice filter as last filter in order to make sure that filters and servlets
         *  configured in Guice web module can be filtered correctly by filters added to 
         *  Jetty context directly.  
         */
        contextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        GzipHandler gzipHandler = new GzipHandler();
		gzipHandler.setIncludedMimeTypes("text/css", "application/javascript", "text/javascript");
		gzipHandler.setHandler(contextHandler);

        server.setHandler(gzipHandler);

        for (ServerConfigurator configurator: serverConfiguratorsProvider.get()) 
        	configurator.configure(server);
		
        return server;
	}

}
