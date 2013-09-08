package com.pmease.gitop.product;

import java.io.File;
import java.util.Collection;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.jetty.FileAssetServlet;
import com.pmease.commons.jetty.extensionpoints.ServerConfigurator;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.gitop.core.setting.ServerConfig;
import com.pmease.gitop.core.setting.SslConfig;

public class Product extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Product.class);
	
	private final ServerConfig serverConfig;

	public static final String NAME = "Gitop";
	
	@Inject
	public Product(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public Collection<?> getExtensions() {
		return ImmutableList.of(				
			new ServerConfigurator() {
		
				@SuppressWarnings("deprecation")
				@Override
				public void configure(Server server) {
					if (serverConfig.getHttpPort() != 0) {
						SocketConnector connector = new SocketConnector();
						connector.setPort(serverConfig.getHttpPort());
						server.addConnector(connector);
					}

					SslConfig sslConfig = serverConfig.getSslConfig();
					if (sslConfig != null) {
						SslSocketConnector sslConnector = new SslSocketConnector();
						sslConnector.setPort(sslConfig.getPort());
						
						sslConnector.setKeystore(sslConfig.getKeystorePath());
						sslConnector.setPassword(sslConfig.getKeystorePassword());
						sslConnector.setKeyPassword(sslConfig.getKeystoreKeyPassword());
						
						server.addConnector(sslConnector);
					}
				}
			}, 
			new ServletContextConfigurator() {

				@Override
				public void configure(ServletContextHandler context) {
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
		);
	}
	
	@Override
	public void postStart() {
		logger.info(NAME + " has been started successfully.");
	}

}
