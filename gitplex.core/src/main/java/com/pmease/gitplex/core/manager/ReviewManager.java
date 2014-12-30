package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.User;

public interface ReviewManager {

	Review findBy(User reviewer, PullRequestUpdate update);

	void review(PullRequest request, User reviewer, Review.Result result, @Nullable String comment);
	
	/**
	 * Find list of reviews ordered by review date for specified pull request.
	 * 
	 * @param request
	 * 			pull request to find review for
	 * @return
	 * 			list of reviews ordered by review date
	 */
	List<Review> findBy(PullRequest request);
	
	void delete(Review review);
}
