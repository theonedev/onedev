package com.gitplex.server.core.event;

import com.gitplex.server.core.entity.Account;

public class TaskChangeEvent {

	private Long userId;
	
	public TaskChangeEvent(Account user) {
		userId = user.getId();
	}

	public Long getUserId() {
		return userId;
	}
	
}
