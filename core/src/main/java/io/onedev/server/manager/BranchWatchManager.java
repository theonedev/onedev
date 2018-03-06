package io.onedev.server.manager;

import java.util.Collection;

import io.onedev.server.model.BranchWatch;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface BranchWatchManager extends EntityManager<BranchWatch> {
	
	Collection<BranchWatch> find(User user, Project project);
	
	Collection<BranchWatch> find(Project project, String branch);

}
