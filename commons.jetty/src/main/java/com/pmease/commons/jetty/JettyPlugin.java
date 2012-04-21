package com.pmease.commons.jetty;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;

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
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.Inject;
import com.pmease.commons.bootstrap.BootstrapUtils;
import com.pmease.commons.jetty.extensionpoints.ServerConfigurator;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.PluginManager;

public class JettyPlugin extends AbstractPlugin {
	
	private Server server;
	
	public final PluginManager pluginManager;

	@Inject
	public JettyPlugin(AppLoader appLoader, PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
	
	@Override
	public void preStartDependents() {
		server = createServer();
		
		try {
			server.start();
		} catch (Exception e) {
			throw BootstrapUtils.unchecked(e);
		}
	}

	@Override
	public void postStopDependents() {
		try {
			server.stop();
		} catch (Exception e) {
			throw BootstrapUtils.unchecked(e);
		}
	}

	private Server createServer() {
		server = new Server();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setClassLoader(JettyPlugin.class.getClassLoader());
        
        context.setContextPath("/");
        
        context.addFilter(DisableTraceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        Collection<ServletContextConfigurator> servletContextConfigurators = 
        		pluginManager.getExtensions(ServletContextConfigurator.class);
        for (ServletContextConfigurator configurator: servletContextConfigurators) 
        	configurator.configure(context);
        
        server.setHandler(context);

        Collection<ServerConfigurator> servletContainerConfigurators = 
        		pluginManager.getExtensions(ServerConfigurator.class);
        for (ServerConfigurator configurator: servletContainerConfigurators) 
        	configurator.configure(server);
		
        return server;
	}

	@Override
	public Collection<?> getExtensions() {
		return null;
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
