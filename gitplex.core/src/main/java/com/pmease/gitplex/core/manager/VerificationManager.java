package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Verification;

public interface VerificationManager extends EntityManager<Verification> {
	
	void save(Verification verification);
	
	void delete(Verification verification);
	
	Collection<Verification> findAll(PullRequest request, String commit);
	
	Verification find(PullRequest request, String commit, String configuration);
	
	Verification.Status getOverallStatus(Collection<Verification> verifications);
}
