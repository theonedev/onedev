package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequestComment;

public interface PullRequestCommentManager {

	/**
	 * Update specified inline comment to make sure it is up to date with latest pull request update
	 * 
	 * @param comment
	 *			comment to be updated 
	 */
	void updateInline(PullRequestComment comment);
	
	void save(PullRequestComment comment);
	
}
