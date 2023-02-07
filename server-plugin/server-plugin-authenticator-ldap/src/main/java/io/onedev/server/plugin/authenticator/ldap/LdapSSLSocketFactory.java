package io.onedev.server.plugin.authenticator.ldap;

import io.onedev.server.OneDev;
import nl.altindag.ssl.SSLFactory;

import javax.net.ssl.SSLSocketFactory;

public abstract class LdapSSLSocketFactory extends SSLSocketFactory {
	
	public static SSLSocketFactory getDefault() {
		return OneDev.getInstance(SSLFactory.class).getSslSocketFactory();
	}
	
}
