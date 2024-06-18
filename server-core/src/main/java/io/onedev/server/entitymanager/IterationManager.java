package io.onedev.server.entitymanager;

import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;

public interface IterationManager extends EntityManager<Iteration> {
	
	@Nullable
    Iteration findInHierarchy(Project project, String name);
	
	void delete(Iteration iteration);

	Iteration findInHierarchy(String iterationFQN);

    void createOrUpdate(Iteration iteration);
	
}