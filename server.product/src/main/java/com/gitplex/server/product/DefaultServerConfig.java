package com.gitplex.server.product;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.commons.bootstrap.Bootstrap;
import com.gitplex.commons.util.StringUtils;
import com.gitplex.server.core.setting.ServerConfig;
import com.gitplex.server.core.setting.SslConfig;

@Singleton
public class DefaultServerConfig implements ServerConfig {

	private static final String PROP_HTTPPORT = "httpPort";
	
	private static final String PROP_HTTPSPORT = "httpsPort";
	
	private static final String PROP_KEYSTOREPATH = "keystorePath";
	
	private static final String PROP_KEYSTOREPASSWORD = "keystorePassword";
	
	private static final String PROP_KEYSTOREKEYPASSWORD = "keystoreKeyPassword";
	
	private int httpPort;
	
	private int sessionTimeout;

	private SslConfig sslConfig;
	
	@Inject
	public DefaultServerConfig(ServerProperties props) {
		String httpPortStr = props.getProperty(PROP_HTTPPORT);
		if (StringUtils.isNotBlank(httpPortStr)) 
			httpPort = Integer.parseInt(httpPortStr.trim());
		
		String httpsPortStr = props.getProperty(PROP_HTTPSPORT);
		
		if (StringUtils.isNotBlank(httpsPortStr)) {
			SslConfigBean sslConfigBean = new SslConfigBean();
			sslConfigBean.setPort(Integer.parseInt(httpsPortStr.trim()));
			
			String keystorePath = props.getProperty(PROP_KEYSTOREPATH);
			if (StringUtils.isBlank(keystorePath))
				keystorePath = "sample.keystore";
			else
				keystorePath = keystorePath.trim();
			
			String keystorePassword = props.getProperty(PROP_KEYSTOREPASSWORD);
			if (StringUtils.isBlank(keystorePassword))
				keystorePassword = "123456";
			else
				keystorePassword = keystorePassword.trim();
			
			String keystoreKeyPassword = props.getProperty(PROP_KEYSTOREKEYPASSWORD);
			if (StringUtils.isBlank(keystoreKeyPassword))
				keystoreKeyPassword = "123456";
			else
				keystoreKeyPassword = keystoreKeyPassword.trim();
			
			File keystoreFile = new File(keystorePath);
			if (!keystoreFile.isAbsolute())
				keystoreFile = new File(Bootstrap.getConfDir(), keystorePath);
			
			sslConfigBean.setKeyStorePath(keystoreFile.getAbsolutePath());
			sslConfigBean.setKeyStorePassword(keystorePassword);
			sslConfigBean.setKeyStoreKeyPassword(keystoreKeyPassword);
			
			sslConfig = sslConfigBean;
		}
		
		if (httpPort == 0 && sslConfig == null)
			throw new RuntimeException("Either httpPort or httpsPort or both should be enabled.");
		
		String sessionTimeoutStr = props.getProperty("sessionTimeout");
		if (StringUtils.isNotBlank(sessionTimeoutStr))
			this.sessionTimeout = Integer.parseInt(sessionTimeoutStr.trim());
		else
			throw new RuntimeException("sessionTimeout is not specified.");
		
	}
	
	@Override
	public int getHttpPort() {
		return httpPort;
	}

	@Override
	public SslConfig getSslConfig() {
		return sslConfig;
	}

	@Override
	public int getSessionTimeout() {
		return sessionTimeout;
	}

}
