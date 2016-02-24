package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Verification;

public interface VerificationManager extends Dao {
	
	void save(Verification verification);
	
	void delete(Verification verification);
	
	Collection<Verification> findBy(PullRequest request, String commit);
	
	Verification findBy(PullRequest request, String commit, String configuration);
	
	Verification.Status getOverallStatus(Collection<Verification> verifications);
}
