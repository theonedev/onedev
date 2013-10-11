package com.pmease.gitop.rest;

import javax.inject.Inject;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ServletConfigurator;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

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
