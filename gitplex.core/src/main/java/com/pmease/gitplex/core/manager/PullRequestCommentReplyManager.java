package com.pmease.gitplex.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestCommentReplyManager;
import com.pmease.gitplex.core.model.PullRequestCommentReply;

@ImplementedBy(DefaultPullRequestCommentReplyManager.class)
public interface PullRequestCommentReplyManager {

	void save(PullRequestCommentReply reply);
	
}
