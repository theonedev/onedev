package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultPullRequestWatchManager;
import com.pmease.gitplex.core.model.PullRequestWatch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@ImplementedBy(DefaultPullRequestWatchManager.class)
public interface PullRequestWatchManager {
	
	Collection<PullRequestWatch> findBy(User user, Repository repository);
	
}
