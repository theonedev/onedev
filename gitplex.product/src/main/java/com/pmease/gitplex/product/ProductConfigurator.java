package com.pmease.gitplex.product;

import javax.inject.Inject;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.pmease.commons.jetty.ServerConfigurator;
import com.pmease.gitplex.core.setting.ServerConfig;
import com.pmease.gitplex.core.setting.SslConfig;

public class ProductConfigurator implements ServerConfigurator {

	private ServerConfig serverConfig;
	
	@Inject
	public ProductConfigurator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public void configure(Server server) {
		if (serverConfig.getHttpPort() != 0) {
			ServerConnector connector = new ServerConnector(server);
			connector.setPort(serverConfig.getHttpPort());
			connector.addConnectionFactory(new HttpConnectionFactory());
			server.addConnector(connector);
		}

		SslConfig sslConfig = serverConfig.getSslConfig();
		if (sslConfig != null) {
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(sslConfig.getKeystorePath());
			sslContextFactory.setKeyStorePassword(sslConfig.getKeystorePassword());
			sslContextFactory.setKeyManagerPassword(sslConfig.getKeystoreKeyPassword());
			
			ServerConnector connector = new ServerConnector(server, sslContextFactory);
			connector.setPort(sslConfig.getPort());
			
			server.addConnector(connector);
		}
	}

}
