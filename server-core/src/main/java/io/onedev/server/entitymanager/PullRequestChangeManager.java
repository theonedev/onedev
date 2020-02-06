package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestChangeManager extends EntityManager<PullRequestChange> {

	void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy);
	
	void changeTitle(PullRequest request, String title);
	
}
