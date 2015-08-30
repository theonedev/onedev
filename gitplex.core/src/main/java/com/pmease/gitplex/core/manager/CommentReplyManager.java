package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.CommentReply;

public interface CommentReplyManager {

	void save(CommentReply reply);
	
	Collection<CommentReply> findBy(PullRequest request);
}
