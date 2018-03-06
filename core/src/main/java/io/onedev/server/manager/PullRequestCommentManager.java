package io.onedev.server.manager;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestCommentManager extends EntityManager<PullRequestComment> {

	void save(PullRequestComment comment, boolean notifyListeners);

}
