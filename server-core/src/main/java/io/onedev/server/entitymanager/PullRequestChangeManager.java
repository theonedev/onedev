package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;

public interface PullRequestChangeManager extends EntityManager<PullRequestChange> {

	void create(PullRequestChange change, @Nullable String note);
	
	void changeMergeStrategy(PullRequest request, MergeStrategy mergeStrategy);
	
	void changeTitle(PullRequest request, String title);

	void changeDescription(PullRequest request, @Nullable String description);
	
	void changeTargetBranch(PullRequest request, String targetBranch);
	
}
