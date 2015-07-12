package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.model.BranchWatch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

public interface BranchWatchManager {
	
	Collection<BranchWatch> findBy(User user, Repository repository);
	
	Collection<BranchWatch> findBy(Repository repository, String branch);
	
}
