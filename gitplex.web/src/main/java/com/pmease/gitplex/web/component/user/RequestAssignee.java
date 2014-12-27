package com.pmease.gitplex.web.component.user;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class RequestAssignee implements Serializable {
	
	private final User user;
	
	private final String alias;
	
	public RequestAssignee(User user, @Nullable String alias) {
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
