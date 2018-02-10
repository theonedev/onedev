package com.turbodev.server.product;

import javax.inject.Inject;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.turbodev.server.util.jetty.ServerConfigurator;
import com.turbodev.server.util.serverconfig.ServerConfig;
import com.turbodev.server.util.serverconfig.SslConfig;

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
			
			HttpConfiguration configuration = new HttpConfiguration();
			configuration.addCustomizer(new SecureRequestCustomizer());
			connector.addConnectionFactory(new HttpConnectionFactory(configuration));
			
			server.addConnector(connector);
		}
	}

}
