package com.pmease.gitop.product;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.jetty.FileAssetServlet;
import com.pmease.commons.jetty.ServletContextConfigurator;
import com.pmease.gitop.core.setting.ServerConfig;

public class GitopServletContextConfigurator implements ServletContextConfigurator {

	private final ServerConfig serverConfig;
	
	@Inject
	public GitopServletContextConfigurator(ServerConfig serverConfig) {
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
		File siteDir = new File(Bootstrap.installDir, "site");
		ServletHolder servletHolder = new ServletHolder(new FileAssetServlet(siteDir));
		context.addServlet(servletHolder, "/site/*");
		context.addServlet(servletHolder, "/robots.txt");
	}

}
