package com.turbodev.server.util.serverconfig;

public interface SslConfig {
	int getPort();
	
	String getKeystorePath();
	
	String getKeystorePassword();
	
	String getKeystoreKeyPassword();
}
