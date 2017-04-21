package com.gitplex.server.util;

import java.util.List;
import java.util.Map;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Review;

public interface ReviewCheckStatus {

	List<Account> getAwaitingReviewers();

	Map<Account, Review> getEffectiveReviews();
	
}
