package io.onedev.server.plugin.sso.openid;

import java.io.Serializable;

public class ProviderMetadata implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String issuer;
	
	private final String authorizationEndpoint;
	
	private final String tokenEndpoint;
	
	private final String userInfoEndpoint;
	
	public ProviderMetadata(String issuer, String authorizationEndpoint, 
			String tokenEndpoint, String userInfoEndpoint) {
		this.issuer = issuer;
		this.authorizationEndpoint = authorizationEndpoint;
		this.tokenEndpoint = tokenEndpoint;
		this.userInfoEndpoint = userInfoEndpoint;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getAuthorizationEndpoint() {
		return authorizationEndpoint;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public String getUserInfoEndpoint() {
		return userInfoEndpoint;
	}
	
}