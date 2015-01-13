package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequestUpdate;

public interface PullRequestUpdateManager {
	
	void save(PullRequestUpdate update, boolean notify);
	
	void delete(PullRequestUpdate update);
}
