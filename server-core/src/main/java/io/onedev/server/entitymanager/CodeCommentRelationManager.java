package io.onedev.server.entitymanager;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentRelation;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.dao.EntityManager;

public interface CodeCommentRelationManager extends EntityManager<CodeCommentRelation> {
	
	CodeCommentRelation find(PullRequest request, CodeComment comment);
	
}
