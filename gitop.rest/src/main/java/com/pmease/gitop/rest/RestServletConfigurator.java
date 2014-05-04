package com.pmease.gitop.rest;

import javax.inject.Inject;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.pmease.commons.jetty.ServletConfigurator;

public class RestServletConfigurator implements ServletConfigurator {

	private final ResourceConfig resourceConfig;
	
	@Inject
	public RestServletConfigurator(ResourceConfig resourceConfig) {
		this.resourceConfig = resourceConfig;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
		
		context.addServlet(servletHolder, RestModule.SERVLET_PATH + "/*");
	}

}
