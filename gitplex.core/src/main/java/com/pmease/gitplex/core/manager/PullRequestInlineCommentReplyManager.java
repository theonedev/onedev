package com.pmease.gitplex.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestInlineCommentReplyManager;
import com.pmease.gitplex.core.model.PullRequestInlineCommentReply;

@ImplementedBy(DefaultPullRequestInlineCommentReplyManager.class)
public interface PullRequestInlineCommentReplyManager {

	void save(PullRequestInlineCommentReply reply);
	
}
