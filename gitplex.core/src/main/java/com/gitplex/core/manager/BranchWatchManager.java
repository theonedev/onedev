package com.gitplex.core.manager;

import java.util.Collection;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.BranchWatch;
import com.gitplex.core.entity.Depot;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(Account user, Depot depot);
	
	Collection<BranchWatch> find(Depot depot, String branch);

}
