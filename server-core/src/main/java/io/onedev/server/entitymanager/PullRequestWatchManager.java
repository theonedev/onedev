package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.watch.WatchStatus;

import java.util.Collection;

public interface PullRequestWatchManager extends EntityManager<PullRequestWatch> {
	
	@Nullable
	PullRequestWatch find(PullRequest request, User user);
	
	void watch(PullRequest request, User user, boolean watching);

    void create(PullRequestWatch watch);

	void update(PullRequestWatch watch);

	void setWatchStatus(User user, Collection<PullRequest> requests, WatchStatus watchStatus);
	
}
