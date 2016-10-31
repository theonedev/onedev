package com.gitplex.core.manager;

import java.util.List;

import com.gitplex.core.entity.CodeComment;
import com.gitplex.core.entity.CodeCommentRelation;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface CodeCommentRelationManager extends EntityManager<CodeCommentRelation> {
	
	/**
	 * Get code comments related to specified pull request, ordered by id descendantly
	 */
	List<CodeComment> findCodeComments(PullRequest request);
	
}
