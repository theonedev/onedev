package io.onedev.server.service;

import io.onedev.server.model.ReviewedDiff;
import io.onedev.server.model.User;

import java.util.Map;

public interface ReviewedDiffService extends EntityService<ReviewedDiff> {
	
	Map<String, ReviewedDiff> query(User user, String oldCommitHash, String newCommitHash);

	void createOrUpdate(ReviewedDiff status);
	
}
