package io.onedev.server.model.support.administration.sso;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.support.administration.authenticator.Authenticated;

public class SsoAuthenticated extends Authenticated {
	
	private static final long serialVersionUID = 1L;
	
	private final String subject;

	private final String userName;	
			
	public SsoAuthenticated(String subject, @Nullable String userName, @Nullable String email, @Nullable String fullName, 
							@Nullable Collection<String> groupNames, @Nullable Collection<String> sshKeys) {
		super(email, fullName, groupNames, sshKeys);
		this.subject = subject;
		this.userName = userName;
	}

	public String getSubject() {
		return subject;
	}

	@Nullable
	public String getUserName() {
		return userName;
	}

}
