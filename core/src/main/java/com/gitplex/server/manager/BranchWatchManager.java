package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.BranchWatch;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.persistence.dao.EntityManager;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(Account user, Depot depot);
	
	Collection<BranchWatch> find(Depot depot, String branch);

}
