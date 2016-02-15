package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.model.BranchWatch;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;

public interface BranchWatchManager {
	
	Collection<BranchWatch> findBy(User user, Depot depot);
	
	Collection<BranchWatch> findBy(Depot depot, String branch);
	
}
