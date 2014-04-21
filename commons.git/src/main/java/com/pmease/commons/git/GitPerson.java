package com.pmease.commons.git;

import java.io.Serializable;

import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.*;

@SuppressWarnings("serial")
public class GitPerson implements Serializable {

	private final String name;
	
	private final String emailAddress;
	
	public GitPerson(String name, String emailAddress) {
		this.name = checkNotNull(name, "name");
		this.emailAddress = checkNotNull(emailAddress, "emailAddress");
	}

	public String getName() {
		return name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GitPerson)) {
			return false;
		}
		
		GitPerson rhs = (GitPerson) other;
		return Objects.equal(name, rhs.name)
				&& Objects.equal(emailAddress, rhs.emailAddress);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name, emailAddress);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", name)
				.add("emailAddress", emailAddress)
				.toString();
	}
	
}
