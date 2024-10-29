package io.onedev.server.entitymanager;

import io.onedev.server.model.ReviewedDiff;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Map;

public interface ReviewedDiffManager extends EntityManager<ReviewedDiff> {
	
	Map<String, ReviewedDiff> query(User user, String oldCommitHash, String newCommitHash);

	void createOrUpdate(ReviewedDiff status);
	
}
