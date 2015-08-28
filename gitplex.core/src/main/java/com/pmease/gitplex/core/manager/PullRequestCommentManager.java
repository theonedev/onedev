package com.pmease.gitplex.core.manager;

import com.pmease.commons.git.BlobIdent;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;

public interface PullRequestCommentManager {

	/**
	 * Update specified inline comment to make sure it is up to date with latest pull request update
	 * 
	 * @param comment
	 *			comment to be updated 
	 */
	void updateInlineInfo(PullRequestComment comment);
	
	void save(PullRequestComment comment, boolean notify);
	
	void addInline(PullRequest request, BlobIdent blobInfo, BlobIdent compareWith, int line, String content);
}
