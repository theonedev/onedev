package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.BranchWatch;
import com.pmease.gitplex.core.entity.Depot;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> findBy(Account user, Depot depot);
	
	Collection<BranchWatch> findBy(Depot depot, String branch);
	
}
