package com.gitplex.server.manager;

import java.util.List;

import javax.annotation.Nullable;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.dao.EntityManager;

public interface ReviewManager extends EntityManager<Review> {

	@Override
	void save(Review review);
	
	@Nullable
	Review find(PullRequest request, User user, String commit);
	
	/**
	 * Find list of reviews ordered by review date for specified pull request.
	 * 
	 * @param request
	 * 			pull request to find review for
	 * @return
	 * 			list of reviews ordered by review date
	 */
	List<Review> findAll(PullRequest request);
	
	void delete(User user, PullRequest request);
	
}
