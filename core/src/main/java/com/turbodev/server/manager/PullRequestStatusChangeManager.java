package com.turbodev.server.manager;

import javax.annotation.Nullable;

import com.turbodev.server.model.PullRequestStatusChange;
import com.turbodev.server.persistence.dao.EntityManager;

public interface PullRequestStatusChangeManager extends EntityManager<PullRequestStatusChange> {
	
	void save(PullRequestStatusChange statusChange, @Nullable Object statusData);
	
}
