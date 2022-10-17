package io.onedev.server;

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
	
	@Nullable
	File getKeystoreFile();
	
	String getKeystorePassword();
	
	@Nullable
	File getTrustCertsDir();
	
	/**
     * Get ssh port configured for the server.
     * <p> 
     * @return
     *          ssh port of the server
     */
    int getSshPort();
	
	String getClusterIp();
	
	int getClusterPort();
	
	int getServerCpu();
	
	int getServerMemory();
	
}
