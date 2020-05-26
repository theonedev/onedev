package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestVerification;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestVerificationManager extends EntityManager<PullRequestVerification> {

	void saveVerifications(PullRequest request);
	
	void populateVerifications(Collection<PullRequest> requests);
	
}
