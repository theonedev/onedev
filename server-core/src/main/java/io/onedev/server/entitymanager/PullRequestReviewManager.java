package io.onedev.server.entitymanager;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestReviewManager extends EntityManager<PullRequestReview> {
	
	@Nullable
	PullRequestReview find(PullRequest request, User user, String commit);
	
	void populateReviews(Collection<PullRequest> requests);
	
}
