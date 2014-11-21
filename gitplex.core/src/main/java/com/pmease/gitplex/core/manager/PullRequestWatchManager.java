package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.model.PullRequestWatch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

public interface PullRequestWatchManager {
	
	Collection<PullRequestWatch> findBy(User user, Repository repository);
	
}
