package com.gitplex.server.manager;

import javax.annotation.Nullable;

import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.persistence.dao.EntityManager;

public interface PullRequestStatusChangeManager extends EntityManager<PullRequestStatusChange> {
	
	void save(PullRequestStatusChange statusChange, @Nullable Object statusData);
	
}
