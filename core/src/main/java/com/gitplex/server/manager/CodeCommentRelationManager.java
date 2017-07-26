package com.gitplex.server.manager;

import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.CodeCommentRelation;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.persistence.dao.EntityManager;

public interface CodeCommentRelationManager extends EntityManager<CodeCommentRelation> {
	
	CodeCommentRelation find(PullRequest request, CodeComment comment);
	
}
