package io.onedev.server.security;

import org.apache.shiro.authc.AuthenticationToken;

import io.onedev.server.model.User;

public class BearerAuthenticationToken implements AuthenticationToken {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public BearerAuthenticationToken(User user) {
		this.user = user;
	}
	
	@Override
	public Object getPrincipal() {
		return user;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

}
