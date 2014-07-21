package com.pmease.gitplex.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestUpdateManager;
import com.pmease.gitplex.core.model.PullRequestUpdate;

@ImplementedBy(DefaultPullRequestUpdateManager.class)
public interface PullRequestUpdateManager {
	
	void save(PullRequestUpdate update);
	
	void delete(PullRequestUpdate update);
}
