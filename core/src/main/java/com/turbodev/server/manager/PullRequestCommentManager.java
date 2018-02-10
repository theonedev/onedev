package com.turbodev.server.manager;

import com.turbodev.server.model.PullRequestComment;
import com.turbodev.server.persistence.dao.EntityManager;

public interface PullRequestCommentManager extends EntityManager<PullRequestComment> {

	void save(PullRequestComment comment, boolean notifyListeners);

}
