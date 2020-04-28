package io.onedev.server.model.support.administration.authenticator;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.SshKey;

public class Authenticated {
	
	private final String fullName;
	
	private final String email;
	
	private final Collection<String> groupNames;
	
	private final Collection<SshKey> sshKeys;
	
	public Authenticated(String email, @Nullable String fullName,
			@Nullable Collection<String> groupNames, @Nullable Collection<SshKey> sshKeys) {
		this.email = email;
		this.fullName = fullName;
		this.groupNames = groupNames;
		this.sshKeys = sshKeys;
	}
	
	@Nullable
	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	@Nullable
	public Collection<String> getGroupNames() {
		return groupNames;
	}

	@Nullable
	public Collection<SshKey> getSshKeys() {
		return sshKeys;
	}

}
