package io.onedev.server.product;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.serverconfig.ServerConfig;
import io.onedev.server.util.serverconfig.SslConfig;

@Singleton
public class DefaultServerConfig implements ServerConfig {

	private static final String PROP_HTTPPORT = "http_port";
	
	private static final String PROP_HTTPSPORT = "https_port";
	
	private static final String PROP_KEYSTOREPATH = "keystore_path";
	
	private static final String PROP_KEYSTOREPASSWORD = "keystore_password";
	
	private static final String PROP_KEYSTOREKEYPASSWORD = "keystore_key_password";
	
	private int httpPort;
	
	private int sessionTimeout = 1800;
	
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
		
		String sessionTimeoutStr = props.getProperty("session_timeout");
		if (StringUtils.isNotBlank(sessionTimeoutStr))
			sessionTimeout = Integer.parseInt(sessionTimeoutStr.trim());
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
