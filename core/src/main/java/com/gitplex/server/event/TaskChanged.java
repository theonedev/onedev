package com.gitplex.server.event;

import com.gitplex.server.model.Account;

public class TaskChanged {

	private Long userId;
	
	public TaskChanged(Account user) {
		userId = user.getId();
	}

	public Long getUserId() {
		return userId;
	}
	
}
