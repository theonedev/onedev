package com.pmease.commons.jetty;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;
import com.pmease.commons.bootstrap.BootstrapUtils;
import com.pmease.commons.loader.AbstractPlugin;

public class JettyPlugin extends AbstractPlugin {
	
	private Server server;
	
	private ServletContextHandler contextHandler;
	
	private final Set<ServerConfigurator> serverConfigurators;
	
	private final Set<ServletConfigurator> servletContextConfigurators;
	
	@Inject
	public JettyPlugin(
			Set<ServerConfigurator> serverConfigurators, 
			Set<ServletConfigurator> servletContextConfigurators) {
		this.serverConfigurators = serverConfigurators;
		this.servletContextConfigurators = servletContextConfigurators;
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
		try {
			server.stop();
		} catch (Exception e) {
			throw BootstrapUtils.unchecked(e);
		}
	}
	
	public ServletContextHandler getContextHandler() {
		return contextHandler;
	}
	
	public Server getServer() {
		return server;
	}
	
	private Server createServer() {
		server = new Server();

        contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        
        contextHandler.setClassLoader(JettyPlugin.class.getClassLoader());
        
        contextHandler.setErrorHandler(new ErrorPageErrorHandler());
        
        contextHandler.addFilter(DisableTraceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        for (ServletConfigurator configurator: servletContextConfigurators) 
        	configurator.configure(contextHandler);

        /*
         *  Add Guice filter as last filter in order to make sure that filters and servlets
         *  configured in Guice web module can be filtered correctly by filters added to 
         *  Jetty context directly.  
         */
        contextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        server.setHandler(contextHandler);

        for (ServerConfigurator configurator: serverConfigurators) 
        	configurator.configure(server);
		
        return server;
	}

	public static class DisableTraceFilter implements Filter {

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (httpRequest.getMethod().equals("TRACE")) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			} else {
				chain.doFilter(request, response);
			}
		}

		@Override
		public void destroy() {
		}
		
	}

}
