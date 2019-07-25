package io.onedev.server.product;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.util.serverconfig.ServerConfig;
import io.onedev.server.util.serverconfig.SslConfig;

@Singleton
public class DefaultServerConfig implements ServerConfig {

	private static final String PROP_HTTPPORT = "http_port";
	
	private static final String PROP_HTTPSPORT = "https_port";
	
	private static final String PROP_KEYSTORE = "keystore";
	
	private static final String PROP_KEYSTORE_ENCODING = "keystore_encoding";
	
	private static final String PROP_KEYSTOREPASSWORD = "keystore_password";
	
	private int httpPort;
	
	private int sessionTimeout = 1800;
	
	private SslConfig sslConfig;
	
	@Inject
	public DefaultServerConfig(ServerProperties props) {
		String httpPortStr = System.getenv(PROP_HTTPPORT);
		if (httpPortStr == null)
			httpPortStr = props.getProperty(PROP_HTTPPORT);
		if (httpPortStr != null) 
			httpPort = Integer.parseInt(httpPortStr.trim());

		String httpsPortStr = System.getenv(PROP_HTTPSPORT);
		if (httpsPortStr == null)
			httpsPortStr = props.getProperty(PROP_HTTPSPORT);
		
		if (httpsPortStr != null) {
			SslConfigBean sslConfigBean = new SslConfigBean();
			sslConfigBean.setPort(Integer.parseInt(httpsPortStr.trim()));
			
			String keystorePath = System.getenv(PROP_KEYSTORE); 
			if (keystorePath == null)
				keystorePath = props.getProperty(PROP_KEYSTORE);
			if (keystorePath != null)
				keystorePath = keystorePath.trim();
			else
				throw new OneException("Keystore file is required for https support");
			
			String keystorePassword = System.getenv(PROP_KEYSTOREPASSWORD);
			if (keystorePassword == null)
				keystorePassword = props.getProperty(PROP_KEYSTOREPASSWORD);
			if (keystorePassword != null)
				keystorePassword = keystorePassword.trim();

			File keystoreFile = new File(keystorePath);
			if (!keystoreFile.isAbsolute())
				keystoreFile = new File(Bootstrap.getConfDir(), keystorePath);
			String keystoreEncoding = System.getenv(PROP_KEYSTORE_ENCODING);
			if (keystoreEncoding == null)
				keystoreEncoding = props.getProperty(PROP_KEYSTORE_ENCODING);
			if (keystoreEncoding != null)
				keystoreEncoding = keystoreEncoding.trim();
			if ("base64".equals(keystoreEncoding)) {
				try {
					String content = FileUtils.readFileToString(keystoreFile);
					keystoreFile = File.createTempFile("keystore", "p12");
					FileUtils.writeByteArrayToFile(keystoreFile, Base64.decodeBase64(content));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} 
			
			sslConfigBean.setKeyStorePath(keystoreFile.getAbsolutePath());
			sslConfigBean.setKeyStorePassword(keystorePassword);
			
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
