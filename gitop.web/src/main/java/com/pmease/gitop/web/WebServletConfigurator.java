package com.pmease.gitop.web;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.gitop.web.assets.AssetLocator;
import com.pmease.gitop.web.common.component.fileupload.FileUploadServlet;

@Singleton
public class WebServletConfigurator implements ServletConfigurator {

	private final WicketServlet wicketServlet;

	@Inject
	public WebServletConfigurator(WicketServlet wicketServlet) {
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
		
		servletHolder = new ServletHolder(new ClasspathAssetServlet(AssetLocator.class));
		context.addServlet(servletHolder, "/assets/*");
		context.addServlet(servletHolder, "/favicon.ico");
		
		ErrorPageErrorHandler errorHandler = (ErrorPageErrorHandler) context.getErrorHandler();
		errorHandler.addErrorPage(HttpServletResponse.SC_NOT_FOUND, "/assets/404.html");
		
		servletHolder = context.addServlet(FileUploadServlet.class, "/uploads/*");
	}

}
