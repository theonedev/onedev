package io.onedev.server.model.support.authenticator;

import java.util.Collection;

import javax.annotation.Nullable;

public class Authenticated {
	
	private final String fullName;
	
	private final String email;
	
	private final Collection<String> teamFQNs;
	
	public Authenticated(@Nullable String fullName, @Nullable String email, Collection<String> teamFQNs) {
		this.fullName = fullName;
		this.email = email;
		this.teamFQNs = teamFQNs;
	}

	@Nullable
	public String getFullName() {
		return fullName;
	}

	@Nullable
	public String getEmail() {
		return email;
	}

	public Collection<String> getTeamFQNs() {
		return teamFQNs;
	}
	
}
