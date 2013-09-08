package com.pmease.gitop.product;

import com.pmease.gitop.core.setting.SslConfig;

class SslConfigBean implements SslConfig {

	private int port;
	
	private String keyStorePath;
	
	private String keyStorePassword;
	
	private String keyStoreKeyPassword;

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getKeystorePath() {
		return keyStorePath;
	}

	@Override
	public String getKeystorePassword() {
		return keyStorePassword;
	}

	@Override
	public String getKeystoreKeyPassword() {
		return keyStoreKeyPassword;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public void setKeyStoreKeyPassword(String keyStoreKeyPassword) {
		this.keyStoreKeyPassword = keyStoreKeyPassword;
	}

}
