package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.core.manager.impl.DefaultVerificationManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Verification;

@ImplementedBy(DefaultVerificationManager.class)
public interface VerificationManager {
	
	void save(Verification verification);
	
	void delete(Verification verification);
	
	Collection<Verification> findBy(PullRequest request, String commit);
	
	Verification findBy(PullRequest request, String commit, String configuration);
	
	Verification.Status getOverallStatus(Collection<Verification> verifications);
}
