package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequestUpdate;

public interface PullRequestUpdateManager {
	
	void save(PullRequestUpdate update);
	
	void delete(PullRequestUpdate update);
}
