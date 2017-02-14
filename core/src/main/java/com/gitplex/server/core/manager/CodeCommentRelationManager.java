package com.gitplex.server.core.manager;

import java.util.List;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.CodeComment;
import com.gitplex.server.core.entity.CodeCommentRelation;
import com.gitplex.server.core.entity.PullRequest;

public interface CodeCommentRelationManager extends EntityManager<CodeCommentRelation> {
	
	/**
	 * Get code comments related to specified pull request, ordered by id descendantly
	 */
	List<CodeComment> findCodeComments(PullRequest request);
	
}
