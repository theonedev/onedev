package io.onedev.server.service;

import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;

import org.jspecify.annotations.Nullable;

public interface IterationService extends EntityService<Iteration> {
	
	@Nullable
    Iteration findInHierarchy(Project project, String name);
	
	void delete(Iteration iteration);

	Iteration findInHierarchy(String iterationFQN);

    void createOrUpdate(Iteration iteration);
	
}