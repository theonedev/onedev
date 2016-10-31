package com.gitplex.core.manager;

import com.gitplex.core.entity.PullRequestComment;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface PullRequestCommentManager extends EntityManager<PullRequestComment> {

	void save(PullRequestComment comment, boolean notifyListeners);

}
