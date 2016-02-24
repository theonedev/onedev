package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.BranchWatch;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.User;

public interface BranchWatchManager extends Dao {
	
	Collection<BranchWatch> findBy(User user, Depot depot);
	
	Collection<BranchWatch> findBy(Depot depot, String branch);
	
}
