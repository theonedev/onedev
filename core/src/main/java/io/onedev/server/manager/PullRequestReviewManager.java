package io.onedev.server.manager;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestReviewManager extends EntityManager<PullRequestReview> {

	void review(PullRequestReview review);
	
	@Nullable
	PullRequestReview find(PullRequest request, User user, String commit);
	
	boolean excludeReviewer(PullRequestReview review);
	
	void addReviewer(PullRequestReview review);
	
	void saveReviews(PullRequest request);
}
