package io.onedev.server.product;

import javax.inject.Inject;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.ServerConfig;
import io.onedev.server.jetty.ServerConfigurator;

public class ProductConfigurator implements ServerConfigurator {

	private static final int REQUEST_HEADER_SIZE = 16*1024;
	
	private static final Logger logger = LoggerFactory.getLogger(ProductConfigurator.class);
	
	private ServerConfig serverConfig;
	
	@Inject
	public ProductConfigurator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public void configure(Server server) {
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setRequestHeaderSize(REQUEST_HEADER_SIZE);
		configuration.setSendServerVersion(false);
		
		ServerConnector connector = new ServerConnector(server);
		connector.setHost(serverConfig.getHttpHost());
		connector.setPort(serverConfig.getHttpPort());
		connector.addConnectionFactory(new HttpConnectionFactory(configuration));
		server.addConnector(connector);
		
		// If http_host is not a wildcard (0.0.0.0 or ::), also bind to both IPv4 and IPv6 
		// loopback addresses as curl needs to access localhost for git operations
		String httpHost = serverConfig.getHttpHost();
		
		if (!"0.0.0.0".equals(httpHost) && !"::".equals(httpHost)) {
			if (!"127.0.0.1".equals(httpHost)) {
				var localhostConnector = new ServerConnector(server);
				localhostConnector.setHost("127.0.0.1");
				localhostConnector.setPort(serverConfig.getHttpPort());
				localhostConnector.addConnectionFactory(new HttpConnectionFactory(configuration));
				server.addConnector(localhostConnector);					
			}			
			if (!"::1".equals(httpHost)) {
				try {
					var localhostConnector = new ServerConnector(server);
					localhostConnector.setHost("::1");
					localhostConnector.setPort(serverConfig.getHttpPort());
					localhostConnector.addConnectionFactory(new HttpConnectionFactory(configuration));
					server.addConnector(localhostConnector);
				} catch (Exception e) {
					logger.debug("Unable to bind to IPv6 loopback address ::1, IPv6 may not be available", e);
				}
			}
		}

	}

}
