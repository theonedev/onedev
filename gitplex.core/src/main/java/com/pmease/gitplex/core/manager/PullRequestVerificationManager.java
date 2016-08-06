package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestVerification;

public interface PullRequestVerificationManager extends EntityManager<PullRequestVerification> {
	
	Collection<PullRequestVerification> findAll(PullRequest request, String commit);
	
	PullRequestVerification find(PullRequest request, String commit, String configuration);
	
	PullRequestVerification.Status getOverallStatus(Collection<PullRequestVerification> verifications);
}
