package com.gitplex.server.core.manager;

import java.util.Collection;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.BranchWatch;
import com.gitplex.server.core.entity.Depot;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(Account user, Depot depot);
	
	Collection<BranchWatch> find(Depot depot, String branch);

}
