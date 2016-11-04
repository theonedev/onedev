package com.gitplex.server.core.manager;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.PullRequestComment;

public interface PullRequestCommentManager extends EntityManager<PullRequestComment> {

	void save(PullRequestComment comment, boolean notifyListeners);

}
