package io.onedev.server.entitymanager;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestReviewManager extends EntityManager<PullRequestReview> {
	
    void review(PullRequest request, boolean approved, @Nullable String note);
	
	void populateReviews(Collection<PullRequest> requests);

    void create(PullRequestReview review);

	void update(PullRequestReview review);
	
}
