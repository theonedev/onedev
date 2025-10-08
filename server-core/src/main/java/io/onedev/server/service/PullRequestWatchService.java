package io.onedev.server.service;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.util.watch.WatchStatus;

import java.util.Collection;

public interface PullRequestWatchService extends EntityService<PullRequestWatch> {
	
	@Nullable
	PullRequestWatch find(PullRequest request, User user);
	
	void watch(PullRequest request, User user, boolean watching);

    void createOrUpdate(PullRequestWatch watch);

	void setWatchStatus(User user, Collection<PullRequest> requests, WatchStatus watchStatus);
	
}
