package io.onedev.server.model.support.administration.authenticator;

import java.util.Collection;

import javax.annotation.Nullable;

public class Authenticated {
	
	private final String fullName;
	
	private final String email;
	
	private final Collection<String> groupNames;
	
	public Authenticated(@Nullable String fullName, @Nullable String email, Collection<String> groupNames) {
		this.fullName = fullName;
		this.email = email;
		this.groupNames = groupNames;
	}

	@Nullable
	public String getFullName() {
		return fullName;
	}

	@Nullable
	public String getEmail() {
		return email;
	}

	public Collection<String> getGroupNames() {
		return groupNames;
	}
	
}
