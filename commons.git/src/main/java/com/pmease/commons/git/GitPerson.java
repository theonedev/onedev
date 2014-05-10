package com.pmease.commons.git;

import java.io.Serializable;

import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.*;

@SuppressWarnings("serial")
public class GitPerson implements Serializable {

	private final String name;
	
	private final String email;
	
	public GitPerson(String name, String email) {
		this.name = checkNotNull(name, "name");
		this.email = checkNotNull(email, "email");
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GitPerson)) {
			return false;
		}
		
		GitPerson rhs = (GitPerson) other;
		return Objects.equal(name, rhs.name)
				&& Objects.equal(email, rhs.email);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name, email);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", name)
				.add("email", email)
				.toString();
	}
	
}
