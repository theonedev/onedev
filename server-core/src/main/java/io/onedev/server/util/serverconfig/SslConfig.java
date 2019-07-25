package io.onedev.server.util.serverconfig;

public interface SslConfig {
	int getPort();
	
	String getKeystore();
	
	String getKeystorePassword();
}
