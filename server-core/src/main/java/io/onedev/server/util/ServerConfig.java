package io.onedev.server.util;

import java.io.File;
import javax.annotation.Nullable;

public interface ServerConfig {
	
	/**
	 * Get http port configured for the server.
	 * <p> 
	 * @return
	 * 			http port of the server, or <i>0</i> if http port is not defined
	 */
	int getHttpPort();
	
	/**
	 * Get https port configured for the server.
	 * <p> 
	 * @return
	 * 			https port of the server, or <i>0</i> if https port is not defined
	 */
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

	/**
     * Get ssh port configured for the server.
     * <p> 
     * @return
     *          ssh port of the server
     */
    int getSshPort();
	
}
