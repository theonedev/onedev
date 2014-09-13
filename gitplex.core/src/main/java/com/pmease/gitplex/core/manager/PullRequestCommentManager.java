package com.pmease.gitplex.core.manager;

import java.util.List;

import com.google.inject.ImplementedBy;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestCommentManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;

@ImplementedBy(DefaultPullRequestCommentManager.class)
public interface PullRequestCommentManager {
	
	List<PullRequestComment> findByChange(PullRequest request, RevAwareChange change);
	
}
