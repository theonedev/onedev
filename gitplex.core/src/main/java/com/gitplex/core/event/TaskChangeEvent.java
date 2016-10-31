package com.gitplex.core.event;

import com.gitplex.core.entity.Account;

public class TaskChangeEvent {

	private Long userId;
	
	public TaskChangeEvent(Account user) {
		userId = user.getId();
	}

	public Long getUserId() {
		return userId;
	}
	
}
