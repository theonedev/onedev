package com.turbodev.server.manager;

import java.util.Collection;

import com.turbodev.server.model.BranchWatch;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.persistence.dao.EntityManager;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(User user, Project project);
	
	Collection<BranchWatch> find(Project project, String branch);

}
