package com.gitplex.server.core.manager;

import java.util.Collection;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestVerification;

public interface PullRequestVerificationManager extends EntityManager<PullRequestVerification> {
	
	Collection<PullRequestVerification> findAll(PullRequest request, String commit);
	
	PullRequestVerification find(PullRequest request, String commit, String configuration);
	
	PullRequestVerification.Status getOverallStatus(Collection<PullRequestVerification> verifications);
}
