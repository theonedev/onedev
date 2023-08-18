package io.onedev.server.security;

import io.onedev.server.OneDev;
import nl.altindag.ssl.SSLFactory;

import javax.net.ssl.SSLSocketFactory;

public abstract class TrustCertsSSLSocketFactory extends SSLSocketFactory {
	
	public static SSLSocketFactory getDefault() {
		return OneDev.getInstance(SSLFactory.class).getSslSocketFactory();
	}
	
}
