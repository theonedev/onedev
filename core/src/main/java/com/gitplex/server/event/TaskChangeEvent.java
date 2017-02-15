package com.gitplex.server.event;

import com.gitplex.server.entity.Account;

public class TaskChangeEvent {

	private Long userId;
	
	public TaskChangeEvent(Account user) {
		userId = user.getId();
	}

	public Long getUserId() {
		return userId;
	}
	
}
