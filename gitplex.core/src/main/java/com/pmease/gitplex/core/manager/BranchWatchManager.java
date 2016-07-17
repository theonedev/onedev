package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.BranchWatch;
import com.pmease.gitplex.core.entity.Depot;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(Account user, Depot depot);
	
	Collection<BranchWatch> find(Depot depot, String branch);
	
}
