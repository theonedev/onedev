package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.PullRequestComment;

public interface PullRequestCommentManager extends EntityDao<PullRequestComment> {

	void save(PullRequestComment comment, boolean notify);
	
}
