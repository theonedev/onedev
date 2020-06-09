package io.onedev.server.model.support.administration.sso;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.shiro.authc.AuthenticationToken;

import io.onedev.server.model.User;
import io.onedev.server.model.support.SsoInfo;
import io.onedev.server.model.support.administration.authenticator.Authenticated;

public class SsoAuthenticated extends Authenticated implements AuthenticationToken {
	
	private static final long serialVersionUID = 1L;

	private final String subject;
	
	private final String userName;
	
	private final SsoConnector connector;
	
	public SsoAuthenticated(String subject, String userName, String email, @Nullable String fullName,
			@Nullable Collection<String> groupNames, @Nullable Collection<String> sshKeys, 
			SsoConnector connector) {
		super(email, fullName, groupNames, sshKeys);
		this.subject = subject;
		this.userName = userName;
		this.connector = connector;
	}

	public String getSubject() {
		return subject;
	}

	public String getUserName() {
		return userName;
	}

	public SsoConnector getConnector() {
		return connector;
	}

	@Override
	public Object getPrincipal() {
		return userName;
	}

	@Override
	public Object getCredentials() {
		return User.EXTERNAL_MANAGED;
	}

	public SsoInfo getSsoInfo() {
		SsoInfo ssoInfo = new SsoInfo();
		ssoInfo.setConnector(connector.getName());
		ssoInfo.setSubject(subject);
		return ssoInfo;
	}
}
