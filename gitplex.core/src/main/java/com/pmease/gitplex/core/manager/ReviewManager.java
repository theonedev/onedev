package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;

public interface ReviewManager extends EntityManager<Review> {

	Review find(Account reviewer, PullRequestUpdate update);

	void review(PullRequest request, Account reviewer, Review.Result result, @Nullable String comment);
	
	/**
	 * Find list of reviews ordered by review date for specified pull request.
	 * 
	 * @param request
	 * 			pull request to find review for
	 * @return
	 * 			list of reviews ordered by review date
	 */
	List<Review> findAll(PullRequest request);
	
	void delete(Review review);
}
