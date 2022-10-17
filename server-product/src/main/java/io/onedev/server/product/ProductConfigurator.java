package io.onedev.server.product;

import javax.inject.Inject;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import io.onedev.server.ServerConfig;
import io.onedev.server.jetty.ServerConfigurator;

public class ProductConfigurator implements ServerConfigurator {

	private ServerConfig serverConfig;
	
	@Inject
	public ProductConfigurator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public void configure(Server server) {
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverConfig.getHttpPort());
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setRequestHeaderSize(16*1024);
		connector.addConnectionFactory(new HttpConnectionFactory(configuration));
		server.addConnector(connector);
	}

}
