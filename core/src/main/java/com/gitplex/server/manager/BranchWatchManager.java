package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.BranchWatch;
import com.gitplex.server.model.Depot;
import com.gitplex.server.persistence.dao.EntityManager;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(Account user, Depot depot);
	
	Collection<BranchWatch> find(Depot depot, String branch);

}
