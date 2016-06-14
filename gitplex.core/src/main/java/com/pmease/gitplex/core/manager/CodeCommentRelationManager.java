package com.pmease.gitplex.core.manager;

import java.util.List;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.PullRequest;

public interface CodeCommentRelationManager extends EntityDao<CodeCommentRelation> {
	
	List<CodeComment> queryCodeComments(PullRequest request);
	
}
