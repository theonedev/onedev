package com.pmease.gitop.web.page.repository.api;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.pmease.commons.git.GitIdentity;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;

public class GitPerson implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String emailAddress;

	public GitPerson() {
	}
	
	public GitPerson(String name, String emailAddress) {
		this.name = name;
		this.emailAddress = emailAddress;
	}

	public static GitPerson of(GitIdentity user) {
		return new GitPerson(user.getName(), user.getEmail());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Optional<User> asUser() {
		User user = Gitop.getInstance(UserManager.class).findByEmail(getEmailAddress());
		if (user == null) {
			user = Gitop.getInstance(UserManager.class).findByName(getName());
		}
		
		return Optional.fromNullable(user);
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
				.add("email", emailAddress)
				.toString();
	}
}
