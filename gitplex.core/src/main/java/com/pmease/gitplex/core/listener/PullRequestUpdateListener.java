package com.pmease.gitplex.core.listener;

import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface PullRequestUpdateListener {
	
	void onSaveUpdate(PullRequestUpdate update);
	
}
