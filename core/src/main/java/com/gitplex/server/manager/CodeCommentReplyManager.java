package com.gitplex.server.manager;

import com.gitplex.server.model.CodeCommentReply;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.CompareContext;
import com.gitplex.server.persistence.dao.EntityManager;

public interface CodeCommentReplyManager extends EntityManager<CodeCommentReply> {

	void save(CodeCommentReply reply, CompareContext compareContext, PullRequest request);
	
}
