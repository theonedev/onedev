package com.pmease.gitop.product;

import java.io.File;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.setting.ServerConfig;
import com.pmease.gitop.core.setting.SslConfig;

@Singleton
public class DefaultServerConfig implements ServerConfig {

	private int httpPort;
	
	private int sessionTimeout;
	
	private SslConfig sslConfig;
	
	@Inject
	public DefaultServerConfig(@Named("server") Properties props) {
		String httpPortStr = props.getProperty("httpPort");
		if (StringUtils.isNotBlank(httpPortStr)) {
			httpPort = Integer.parseInt(httpPortStr);
		}
		
		String httpsPortStr = props.getProperty("httpsPort");
		
		if (StringUtils.isNotBlank(httpsPortStr)) {
			SslConfigBean sslConfigBean = new SslConfigBean();
			sslConfigBean.setPort(Integer.parseInt(httpsPortStr));
			
			String keystorePath = props.getProperty("sslKeystorePath");
			if (StringUtils.isBlank(keystorePath))
				keystorePath = "sample.keystore";
			String keystorePassword = props.getProperty("sslKeystorePassword");
			if (StringUtils.isBlank(keystorePassword))
				keystorePassword = "123456";
			String keystoreKeyPassword = props.getProperty("sslKeystoreKeyPassword");
			if (StringUtils.isBlank(keystoreKeyPassword))
				keystoreKeyPassword = "123456";
			
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
		
		String sessionTimeout = props.getProperty("sessionTimeout");
		if (StringUtils.isNotBlank(sessionTimeout))
			this.sessionTimeout = Integer.parseInt(sessionTimeout);
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
