package io.onedev.server.manager;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestActionManager extends EntityManager<PullRequestAction> {

	void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy, @Nullable User user);
	
	void changeTitle(PullRequest request, String title, @Nullable User user);
	
}
