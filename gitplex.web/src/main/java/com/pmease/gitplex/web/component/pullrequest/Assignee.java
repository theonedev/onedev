package com.pmease.gitplex.web.component.pullrequest;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class Assignee implements Serializable {
	
	private final User user;
	
	private final String alias;
	
	public Assignee(User user, @Nullable String alias) {
		this.user = user;
		this.alias = alias;
	}

	public User getUser() {
		return user;
	}

	@Nullable
	public String getAlias() {
		return alias;
	}
}
