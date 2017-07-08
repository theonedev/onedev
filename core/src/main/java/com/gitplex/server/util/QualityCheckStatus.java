package com.gitplex.server.util;

import java.util.List;
import java.util.Map;

import com.gitplex.server.model.User;
import com.gitplex.server.model.Review;

public interface QualityCheckStatus {

	List<User> getAwaitingReviewers();

	Map<User, Review> getEffectiveReviews();
	
	List<String> getAwaitingVerifications();
	
	Map<String, Verification> getEffectiveVerifications();
	
}
