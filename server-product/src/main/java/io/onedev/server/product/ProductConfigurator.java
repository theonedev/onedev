package io.onedev.server.product;

import javax.inject.Inject;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.jetty.ServerConfigurator;

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
			HttpConfiguration configuration = new HttpConfiguration();
			configuration.setRequestHeaderSize(16*1024);
			connector.addConnectionFactory(new HttpConnectionFactory(configuration));
			server.addConnector(connector);
		}

		if (serverConfig.getHttpsPort() != 0) {
			SslContextFactory sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setKeyStoreType("pkcs12");
			sslContextFactory.setKeyStorePath(serverConfig.getKeystoreFile().getAbsolutePath());
			sslContextFactory.setKeyStorePassword(serverConfig.getKeystorePassword());
			
			ServerConnector connector = new ServerConnector(server, sslContextFactory);
			connector.setPort(serverConfig.getHttpsPort());
			
			HttpConfiguration configuration = new HttpConfiguration();
			configuration.setRequestHeaderSize(16*1024);
			configuration.addCustomizer(new SecureRequestCustomizer());
			connector.addConnectionFactory(new HttpConnectionFactory(configuration));
			
			server.addConnector(connector);
		}
	}

}
