package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.core.manager.impl.DefaultPullRequestUpdateManager;
import com.pmease.gitop.model.PullRequestUpdate;

@ImplementedBy(DefaultPullRequestUpdateManager.class)
public interface PullRequestUpdateManager {
	
	void save(PullRequestUpdate update);
	
	void delete(PullRequestUpdate update);
}
