package com.pmease.gitplex.core.event;

import com.pmease.gitplex.core.entity.Account;

public class TaskChangeEvent {

	private Long userId;
	
	public TaskChangeEvent(Account user) {
		userId = user.getId();
	}

	public Long getUserId() {
		return userId;
	}
	
}
