package io.onedev.server.product;

import io.onedev.server.util.serverconfig.SslConfig;

class SslConfigBean implements SslConfig {

	private int port;
	
	private String keyStorePath;
	
	private String keyStorePassword;
	
	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getKeystore() {
		return keyStorePath;
	}

	@Override
	public String getKeystorePassword() {
		return keyStorePassword;
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

}
