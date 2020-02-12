package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestWatchManager extends EntityManager<PullRequestWatch> {
	
	@Nullable
	PullRequestWatch find(PullRequest request, User user);
	
	void watch(PullRequest request, User user, boolean watching);
}
