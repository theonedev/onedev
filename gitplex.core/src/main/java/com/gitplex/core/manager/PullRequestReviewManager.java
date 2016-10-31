package com.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestReview;
import com.gitplex.core.entity.PullRequestUpdate;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface PullRequestReviewManager extends EntityManager<PullRequestReview> {

	PullRequestReview find(Account user, PullRequestUpdate update);

	void review(PullRequest request, PullRequestReview.Result result, @Nullable String note);
	
	/**
	 * Find list of reviews ordered by review date for specified pull request.
	 * 
	 * @param request
	 * 			pull request to find review for
	 * @return
	 * 			list of reviews ordered by review date
	 */
	List<PullRequestReview> findAll(PullRequest request);
	
	void deleteAll(Account user, PullRequest request);
	
}
