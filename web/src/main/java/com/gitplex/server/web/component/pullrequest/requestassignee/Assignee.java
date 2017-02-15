package com.gitplex.server.web.component.pullrequest.requestassignee;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.gitplex.server.entity.Account;

@SuppressWarnings("serial")
public class Assignee implements Serializable {
	
	private final Account user;
	
	private final String alias;
	
	public Assignee(Account user, @Nullable String alias) {
		this.user = user;
		this.alias = alias;
	}

	public Account getUser() {
		return user;
	}

	@Nullable
	public String getAlias() {
		return alias;
	}

}
