package com.gitplex.server.manager;

import java.util.List;

import com.gitplex.server.entity.CodeComment;
import com.gitplex.server.entity.CodeCommentRelation;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.persistence.dao.EntityManager;

public interface CodeCommentRelationManager extends EntityManager<CodeCommentRelation> {
	
	/**
	 * Get code comments related to specified pull request, ordered by id descendantly
	 */
	List<CodeComment> findCodeComments(PullRequest request);
	
}
