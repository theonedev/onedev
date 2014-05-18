package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.core.manager.impl.DefaultCommitVerificationManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.CommitVerification;

@ImplementedBy(DefaultCommitVerificationManager.class)
public interface CommitVerificationManager {
	
	void save(CommitVerification commitVerification);
	
	void delete(CommitVerification commitVerification);
	
	Collection<CommitVerification> findBy(Branch branch, String commit);
	
	CommitVerification findBy(Branch branch, String commit, String configuration);
}
