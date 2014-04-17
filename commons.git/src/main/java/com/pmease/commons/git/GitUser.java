package com.pmease.commons.git;

import java.io.Serializable;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class GitUser implements Serializable {
	
	private final String name;
	
	private final String email;
	
	public GitUser(String name, String email) {
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
		if (!(other instanceof GitUser))
			return false;
		if (this == other)
			return true;
		GitUser otherUser = (GitUser) other;
		return Objects.equal(name, otherUser.name) 
				&& Objects.equal(email, otherUser.email);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", name)
				.add("email", email)
				.toString();
	}
	
}
