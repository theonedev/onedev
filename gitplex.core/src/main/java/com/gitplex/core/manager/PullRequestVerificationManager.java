package com.gitplex.core.manager;

import java.util.Collection;

import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestVerification;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface PullRequestVerificationManager extends EntityManager<PullRequestVerification> {
	
	Collection<PullRequestVerification> findAll(PullRequest request, String commit);
	
	PullRequestVerification find(PullRequest request, String commit, String configuration);
	
	PullRequestVerification.Status getOverallStatus(Collection<PullRequestVerification> verifications);
}
