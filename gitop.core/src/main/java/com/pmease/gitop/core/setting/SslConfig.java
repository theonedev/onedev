package com.pmease.gitop.core.setting;

public interface SslConfig {
	int getPort();
	
	String getKeystorePath();
	
	String getKeystorePassword();
	
	String getKeystoreKeyPassword();
}
