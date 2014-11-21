package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestCommentReply;

public interface PullRequestCommentReplyManager {

	void save(PullRequestCommentReply reply);
	
	Collection<PullRequestCommentReply> findBy(PullRequest request);
}
