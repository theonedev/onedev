package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultCommitWatchManager;
import com.pmease.gitplex.core.model.CommitWatch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@ImplementedBy(DefaultCommitWatchManager.class)
public interface CommitWatchManager {
	
	Collection<CommitWatch> findBy(User user, Repository repository);
	
}
