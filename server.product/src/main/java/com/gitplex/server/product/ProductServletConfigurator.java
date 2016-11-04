package com.gitplex.server.product;

import javax.inject.Inject;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.gitplex.commons.bootstrap.Bootstrap;
import com.gitplex.commons.jetty.FileAssetServlet;
import com.gitplex.commons.jetty.ServletConfigurator;
import com.gitplex.server.core.setting.ServerConfig;

public class ProductServletConfigurator implements ServletConfigurator {

	private final ServerConfig serverConfig;
	
	@Inject
	public ProductServletConfigurator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.setContextPath("/");
		
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
