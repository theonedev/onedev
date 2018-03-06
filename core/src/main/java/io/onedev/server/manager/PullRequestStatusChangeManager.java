package io.onedev.server.manager;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestStatusChangeManager extends EntityManager<PullRequestStatusChange> {
	
	void save(PullRequestStatusChange statusChange, @Nullable Object statusData);
	
}
