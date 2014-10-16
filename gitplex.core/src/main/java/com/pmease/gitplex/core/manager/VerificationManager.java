package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultVerificationManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestVerification;

@ImplementedBy(DefaultVerificationManager.class)
public interface VerificationManager {
	
	void save(PullRequestVerification verification);
	
	void delete(PullRequestVerification verification);
	
	Collection<PullRequestVerification> findBy(PullRequest request, String commit);
	
	PullRequestVerification findBy(PullRequest request, String commit, String configuration);
	
	PullRequestVerification.Status getOverallStatus(Collection<PullRequestVerification> verifications);
}
