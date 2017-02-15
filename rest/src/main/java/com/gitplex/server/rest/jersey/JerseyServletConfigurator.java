package com.gitplex.server.rest.jersey;

import javax.inject.Inject;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.gitplex.server.util.jetty.ServletConfigurator;

public class JerseyServletConfigurator implements ServletConfigurator {

	private final ResourceConfig resourceConfig;
	
	@Inject
	public JerseyServletConfigurator(ResourceConfig resourceConfig) {
		this.resourceConfig = resourceConfig;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
		
		context.addServlet(servletHolder, "/rest/*");
	}

}
