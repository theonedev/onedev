package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestReviewManager extends EntityManager<PullRequestReview> {

	void review(PullRequestReview review);
	
	@Nullable
	PullRequestReview find(PullRequest request, User user, String commit);
	
	void addReviewer(PullRequestReview review);
	
	boolean removeReviewer(PullRequestReview review, List<User> unpreferableReviewers);
	
	void saveReviews(PullRequest request);
	
	void populateReviews(Collection<PullRequest> requests);
	
}
