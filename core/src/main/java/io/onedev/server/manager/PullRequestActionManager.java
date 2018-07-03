package io.onedev.server.manager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestActionManager extends EntityManager<PullRequestAction> {

	void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy);
	
	void changeTitle(PullRequest request, String title);
	
}
