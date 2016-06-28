package com.pmease.gitplex.core.manager;

import java.util.List;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.PullRequest;

public interface CodeCommentRelationManager extends EntityManager<CodeCommentRelation> {
	
	/**
	 * Get code comments related to specified pull request, ordered by id descendantly
	 */
	List<CodeComment> queryCodeComments(PullRequest request);
	
}
