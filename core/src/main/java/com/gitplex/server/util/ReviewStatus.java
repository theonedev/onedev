package com.gitplex.server.util;

import java.util.List;
import java.util.Map;

import com.gitplex.server.model.User;
import com.gitplex.server.model.Review;

public interface ReviewStatus {

	List<User> getAwaitingReviewers();

	Map<User, Review> getEffectiveReviews();
	
}
