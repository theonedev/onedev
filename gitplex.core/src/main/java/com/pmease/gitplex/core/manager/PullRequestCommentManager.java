package com.pmease.gitplex.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestCommentManager;
import com.pmease.gitplex.core.model.PullRequestComment;

@ImplementedBy(DefaultPullRequestCommentManager.class)
public interface PullRequestCommentManager {

	void save(PullRequestComment comment);
	
}
