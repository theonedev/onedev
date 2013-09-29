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

	private String contextPath;
	
	private SslConfig sslConfig;
	
	@Inject
	public DefaultServerConfig(@Named("server") Properties props) {
		String httpPortStr = props.getProperty("httpPort");
		if (StringUtils.isNotBlank(httpPortStr)) {
			httpPort = Integer.parseInt(httpPortStr.trim());
		}
		
		String httpsPortStr = props.getProperty("httpsPort");
		
		if (StringUtils.isNotBlank(httpsPortStr)) {
			SslConfigBean sslConfigBean = new SslConfigBean();
			sslConfigBean.setPort(Integer.parseInt(httpsPortStr.trim()));
			
			String keystorePath = props.getProperty("sslKeystorePath");
			if (StringUtils.isBlank(keystorePath))
				keystorePath = "sample.keystore";
			else
				keystorePath = keystorePath.trim();
			
			String keystorePassword = props.getProperty("sslKeystorePassword");
			if (StringUtils.isBlank(keystorePassword))
				keystorePassword = "123456";
			else
				keystorePassword = keystorePassword.trim();
			
			String keystoreKeyPassword = props.getProperty("sslKeystoreKeyPassword");
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
		
		contextPath = props.getProperty("contextPath");
		if (StringUtils.isNotBlank(contextPath)) {
			contextPath = StringUtils.stripStart(contextPath.trim(), "/ ");
			contextPath = StringUtils.stripEnd(contextPath, "/ ");
			contextPath = "/" + contextPath;
		} else {
			contextPath = "/";
		}
		
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

	@Override
	public String getContextPath() {
		return contextPath;
	}
}
