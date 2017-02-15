package com.gitplex.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestReview;
import com.gitplex.server.entity.PullRequestUpdate;
import com.gitplex.server.persistence.dao.EntityManager;

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
