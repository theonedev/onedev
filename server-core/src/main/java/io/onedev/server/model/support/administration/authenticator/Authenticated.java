package io.onedev.server.model.support.administration.authenticator;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.SshKey;

public class Authenticated {
	
	private final String fullName;
	
	private final String email;
	
	private final Collection<String> groupNames;
	
	private final Collection<SshKey> sshPublicKeys;
	
	public Authenticated(String email, @Nullable String fullName, Collection<String> groupNames) {
		this(email, fullName, groupNames, null);
	}

	public Authenticated(
			String email, @Nullable String fullName,
			Collection<String> groupNames, @Nullable Collection<SshKey> sshPublicKeys
	) {
		this.email = email;
		this.fullName = fullName;
		this.groupNames = groupNames;
		this.sshPublicKeys = sshPublicKeys;
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

	@Nullable
	public Collection<SshKey> getSSHPublicKeys() {
		return sshPublicKeys;
	}

}
