package io.onedev.server.product;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Preconditions;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.ServerConfig;

@Singleton
public class DefaultServerConfig implements ServerConfig {

	private static final String PROP_HTTPPORT = "http_port";
	
	private static final String PROP_HTTPSPORT = "https_port";

	private static final String PROP_SSHPORT = "ssh_port";
	
	private static final String PROP_KEYSTORE = "keystore";
	
	private static final String PROP_TRUST_CERTS = "trust_certs";
	
	private static final String PROP_KEYSTORE_ENCODING = "keystore_encoding";
	
	private static final String PROP_KEYSTOREPASSWORD = "keystore_password";
	
	private static final String PROP_SESSION_TIMEOUT = "session_timeout";
	
	private int httpPort;
	
	private int sessionTimeout;
	
	private int httpsPort;

	private int sshPort;
	
	private File trustCertsDir;
	
	private File keystoreFile;
	
	private String keystorePassword;
	
	@Inject
	public DefaultServerConfig(ServerProperties props) {
		String httpPortStr = System.getenv(PROP_HTTPPORT);
		if (StringUtils.isBlank(httpPortStr))
			httpPortStr = props.getProperty(PROP_HTTPPORT);
		if (StringUtils.isNotBlank(httpPortStr)) 
			httpPort = Integer.parseInt(httpPortStr.trim());

		String httpsPortStr = System.getenv(PROP_HTTPSPORT);
		if (StringUtils.isBlank(httpsPortStr))
			httpsPortStr = props.getProperty(PROP_HTTPSPORT);
		if (StringUtils.isNotBlank(httpsPortStr))
			httpsPort = Integer.parseInt(httpsPortStr.trim());
		
		if (httpPort == 0 && httpsPort == 0)
			throw new ExplicitException("Either " + PROP_HTTPPORT + " or " + PROP_HTTPSPORT + " or both should be enabled");
		
		String sshPortStr = System.getenv(PROP_SSHPORT);
        if (StringUtils.isBlank(sshPortStr))
            sshPortStr = props.getProperty(PROP_SSHPORT);
        if (StringUtils.isNotBlank(sshPortStr))
            sshPort = Integer.parseInt(sshPortStr.trim());
        else
        	throw new ExplicitException(PROP_SSHPORT + " should be specified");
		
		String keystore = System.getenv(PROP_KEYSTORE); 
		if (StringUtils.isBlank(keystore))
			keystore = props.getProperty(PROP_KEYSTORE);
		if (StringUtils.isNotBlank(keystore)) {
			keystoreFile = new File(keystore.trim());
			if (!keystoreFile.isAbsolute())
				keystoreFile = new File(Bootstrap.getConfDir(), keystore);
			
			Preconditions.checkState(keystoreFile.exists(), 
					"Keystore file not exist: " + keystoreFile.getAbsolutePath());
			
			String keystoreEncoding = System.getenv(PROP_KEYSTORE_ENCODING);
			if (keystoreEncoding == null)
				keystoreEncoding = props.getProperty(PROP_KEYSTORE_ENCODING);
			if (keystoreEncoding != null)
				keystoreEncoding = keystoreEncoding.trim();
			if ("base64".equals(keystoreEncoding)) {
				try {
					String content = FileUtils.readFileToString(keystoreFile, StandardCharsets.UTF_8);
					keystoreFile = File.createTempFile("keystore", "pfx");
					FileUtils.writeByteArrayToFile(keystoreFile, Base64.decodeBase64(content));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} 
		} else if (httpsPort != 0) {
			throw new ExplicitException(PROP_KEYSTORE + " is required for https support");
		}
		
		keystorePassword = System.getenv(PROP_KEYSTOREPASSWORD);
		if (keystorePassword == null)
			keystorePassword = props.getProperty(PROP_KEYSTOREPASSWORD);
		if (keystorePassword == null)
			keystorePassword = "";

		String trustCerts = System.getenv(PROP_TRUST_CERTS);
		if (StringUtils.isBlank(trustCerts))
			trustCerts = props.getProperty(PROP_TRUST_CERTS);
		if (StringUtils.isNotBlank(trustCerts)) {
			trustCertsDir = new File(trustCerts.trim());
			if (!trustCertsDir.isAbsolute())
				trustCertsDir = new File(Bootstrap.getConfDir(), trustCerts);
			Preconditions.checkState(trustCertsDir.exists(), 
					"Trust certs directory not exist: " + trustCertsDir.getAbsolutePath());
		}
		
		String sessionTimeoutStr = System.getenv(PROP_SESSION_TIMEOUT);
		if (StringUtils.isBlank(sessionTimeoutStr))
			sessionTimeoutStr = props.getProperty(PROP_SESSION_TIMEOUT);
		if (StringUtils.isNotBlank(sessionTimeoutStr))
			sessionTimeout = Integer.parseInt(sessionTimeoutStr.trim());
		else
			throw new ExplicitException(PROP_SESSION_TIMEOUT + " should be specified");
	}
	
	@Override
	public int getHttpPort() {
		return httpPort;
	}

	@Override
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	@Override
	public int getHttpsPort() {
		return httpsPort;
	}

	@Override
	public File getKeystoreFile() {
		return keystoreFile;
	}

	@Override
	public String getKeystorePassword() {
		return keystorePassword;
	}

	@Override
	public File getTrustCertsDir() {
		return trustCertsDir;
	}

	@Override
    public int getSshPort() {
        return sshPort;
    }
	
}
