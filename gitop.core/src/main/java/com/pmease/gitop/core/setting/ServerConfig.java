package com.pmease.gitop.core.setting;

public interface ServerConfig {
	
	/**
	 * Get http port configured for the server.
	 * <p> 
	 * @return
	 * 			http port of the server, or <i>0</i> if http port is not defined. 
	 * 			In case http port is not defined, {@link #getSslConfig()} must 
	 * 			not return <i>null</i>
	 */
	int getHttpPort();
	
	/**
	 * Get ssl config of the server.
	 * <p>
	 * @return
	 * 			ssl config of the server, or <i>null</i> if ssl setting is not defined. 
	 * 			In case ssl setting is not defined, {@link #getHttpPort()} must not 
	 * 			return <i>0</i>
	 */
	SslConfig getSslConfig();
	
	/**
	 * Get web session timeout in seconds.
	 * <p>
	 * @return
	 * 			web session timeout in seconds
	 */
	int getSessionTimeout();
}
