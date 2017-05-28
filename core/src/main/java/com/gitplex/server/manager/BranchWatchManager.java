package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.model.User;
import com.gitplex.server.model.BranchWatch;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.dao.EntityManager;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(User user, Project project);
	
	Collection<BranchWatch> find(Project project, String branch);

}
