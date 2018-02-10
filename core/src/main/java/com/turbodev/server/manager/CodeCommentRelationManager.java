package com.turbodev.server.manager;

import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.CodeCommentRelation;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.persistence.dao.EntityManager;

public interface CodeCommentRelationManager extends EntityManager<CodeCommentRelation> {
	
	CodeCommentRelation find(PullRequest request, CodeComment comment);
	
}
