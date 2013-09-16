package com.pmease.commons.wicket;

import javax.inject.Inject;

import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ServletContextConfigurator;

public class WicketServletContextConfigurator implements ServletContextConfigurator {

	private final WicketServlet wicketServlet;
	
	@Inject
	public WicketServletContextConfigurator(WicketServlet wicketServlet) {
		this.wicketServlet = wicketServlet;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(wicketServlet);
		
		/*
		 * Add wicket servlet as the default servlet which will serve all requests failed to 
		 * match a path pattern
		 */
		context.addServlet(servletHolder, "/");
	}

}
