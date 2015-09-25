package com.pmease.commons.wicket;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.wicket.behavior.markdown.AttachmentUploadServlet;

@Singleton
public class WicketServletConfigurator implements ServletConfigurator {

	private final WicketServlet wicketServlet;

	@Inject
	public WicketServletConfigurator(WicketServlet wicketServlet) {
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
		context.addServlet(AttachmentUploadServlet.class, "/attachment_upload");
	}

}
