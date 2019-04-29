package io.onedev.server.util.serverconfig;

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
	
	/**
	 * Get ssl config of the server.
	 * <p>
	 * @return
	 * 			ssl config of the server, or <tt>null</tt> if ssl setting is not defined. 
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
