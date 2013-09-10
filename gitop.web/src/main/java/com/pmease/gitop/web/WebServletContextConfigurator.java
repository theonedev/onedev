package com.pmease.gitop.web;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.gitop.web.asset.AssetLocator;

public class WebServletContextConfigurator implements ServletContextConfigurator {

	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(new ClasspathAssetServlet(AssetLocator.class));
		context.addServlet(servletHolder, "/asset/*");
		context.addServlet(servletHolder, "/favicon.ico");
		
		ErrorPageErrorHandler errorHandler = (ErrorPageErrorHandler) context.getErrorHandler();
		errorHandler.addErrorPage(HttpServletResponse.SC_NOT_FOUND, "/asset/404.html");
	}

}
