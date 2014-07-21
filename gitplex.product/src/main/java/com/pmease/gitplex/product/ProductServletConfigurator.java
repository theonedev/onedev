package com.pmease.gitplex.product;

import javax.inject.Inject;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.jetty.FileAssetServlet;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.gitplex.core.setting.ServerConfig;

public class ProductServletConfigurator implements ServletConfigurator {

	private final ServerConfig serverConfig;
	
	@Inject
	public ProductServletConfigurator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.setContextPath(serverConfig.getContextPath());
		
		context.getSessionHandler().getSessionManager().setMaxInactiveInterval(serverConfig.getSessionTimeout());
		
		/*
		 * Configure a servlet to serve contents under site folder. Site folder can be used 
		 * to hold site specific web assets.   
		 */
		ServletHolder servletHolder = new ServletHolder(new FileAssetServlet(Bootstrap.getSiteDir()));
		context.addServlet(servletHolder, "/site/*");
		context.addServlet(servletHolder, "/robots.txt");
	}

}
