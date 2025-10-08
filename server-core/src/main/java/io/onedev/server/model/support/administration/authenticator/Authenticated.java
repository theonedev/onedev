package io.onedev.server.model.support.administration.authenticator;

import java.io.Serializable;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

public class Authenticated implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String fullName;
	
	private final String email;
	
	private final Collection<String> groupNames;
	
	private final Collection<String> sshKeys;
	
	public Authenticated(@Nullable String email, @Nullable String fullName, 
						 @Nullable Collection<String> groupNames, 
						 @Nullable Collection<String> sshKeys) {
		this.email = email;
		this.fullName = fullName;
		this.groupNames = groupNames;
		this.sshKeys = sshKeys;
	}
	
	@Nullable
	public String getFullName() {
		return fullName;
	}

	@Nullable
	public String getEmail() {
		return email;
	}

	@Nullable
	public Collection<String> getGroupNames() {
		return groupNames;
	}

	@Nullable
	public Collection<String> getSshKeys() {
		return sshKeys;
	}

}
