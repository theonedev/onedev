package com.pmease.gitplex.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestInlineCommentManager;
import com.pmease.gitplex.core.model.PullRequestInlineComment;

@ImplementedBy(DefaultPullRequestInlineCommentManager.class)
public interface PullRequestInlineCommentManager {
		
	/**
	 * Update specified inline comment to make sure it is up to date with latest pull request update
	 * 
	 * @param comment
	 *			comment to be updated 
	 */
	void update(PullRequestInlineComment comment);
}
