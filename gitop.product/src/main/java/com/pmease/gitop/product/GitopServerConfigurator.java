package com.pmease.gitop.product;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;

import com.pmease.commons.jetty.extensionpoints.ServerConfigurator;
import com.pmease.gitop.core.setting.ServerConfig;
import com.pmease.gitop.core.setting.SslConfig;

public class GitopServerConfigurator implements ServerConfigurator {

	private ServerConfig serverConfig;
	
	@Inject
	public GitopServerConfigurator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
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

}
