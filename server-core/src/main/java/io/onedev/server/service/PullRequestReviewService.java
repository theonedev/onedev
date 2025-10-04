package io.onedev.server.service;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;

public interface PullRequestReviewService extends EntityService<PullRequestReview> {
	
    void review(User user, PullRequest request, boolean approved, @Nullable String note);
	
	void populateReviews(Collection<PullRequest> requests);

    void createOrUpdate(User user, PullRequestReview review);
	
}
