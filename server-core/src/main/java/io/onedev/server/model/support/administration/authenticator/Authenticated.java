package io.onedev.server.model.support.administration.authenticator;

import java.util.Collection;

import javax.annotation.Nullable;

public class Authenticated {
	
	private final String fullName;
	
	private final String email;
	
	private final Collection<String> groupNames;
	
	public Authenticated(String email, @Nullable String fullName, Collection<String> groupNames) {
		this.email = email;
		this.fullName = fullName;
		this.groupNames = groupNames;
	}

	@Nullable
	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public Collection<String> getGroupNames() {
		return groupNames;
	}
	
}
