package com.pmease.commons.git;

import java.io.Serializable;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class GitIdentity implements Serializable {
	
	private final String name;
	
	private final String email;
	
	public GitIdentity(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, email);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GitIdentity))
			return false;
		if (this == other)
			return true;
		GitIdentity otherIdentity = (GitIdentity) other;
		return Objects.equal(name, otherIdentity.name) 
				&& Objects.equal(email, otherIdentity.email);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", name)
				.add("email", email)
				.toString();
	}
	
}
