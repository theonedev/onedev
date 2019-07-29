package io.onedev.server.util;

import java.io.File;

import javax.annotation.Nullable;

public interface ServerConfig {
	
	/**
	 * Get http port configured for the server.
	 * <p> 
	 * @return
	 * 			http port of the server, or <i>0</i> if http port is not defined. 
	 * 			In case http port is not defined, {@link #getSslConfig()} must 
	 * 			not return <tt>null</tt>
	 */
	int getHttpPort();
	
	int getHttpsPort();
	
	@Nullable
	File getKeystoreFile();
	
	String getKeystorePassword();
	
	@Nullable
	File getTrustCertsDir();
	
	/**
	 * Get web session timeout in seconds.
	 * <p>
	 * @return
	 * 			web session timeout in seconds
	 */
	int getSessionTimeout();
	
}
