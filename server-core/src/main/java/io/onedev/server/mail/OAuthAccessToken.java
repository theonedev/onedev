package io.onedev.server.mail;

import io.onedev.server.util.OAuthUtils;

public class OAuthAccessToken implements MailCredential {

	private static final long serialVersionUID = 1L;

	private final String tokenEndpoint;
	
	private final String clientId;
	
	private final String clientSecret;
	
	private final String refreshToken;
	
	public OAuthAccessToken(String tokenEndpoint, String clientId, String clientSecret, String refreshToken) {
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.refreshToken = refreshToken;
	}

	@Override
	public String getValue() {
		return OAuthUtils.getAccessToken(tokenEndpoint, clientId, clientSecret, refreshToken);
	}

}
