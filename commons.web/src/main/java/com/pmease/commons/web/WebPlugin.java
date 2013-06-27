package com.pmease.commons.web;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.common.collect.ImmutableList;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;

public class WebPlugin extends AbstractPlugin {

	private final WicketServlet wicketServlet;
	
	@Inject
	public WebPlugin(WicketServlet wicketServlet) {
		this.wicketServlet = wicketServlet;
	}
	
	@Override
	public Collection<?> getExtensions() {
		return ImmutableList.of(
			new ServletContextConfigurator() {
	
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
		);	
	}

}
