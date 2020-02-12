package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface MilestoneManager extends EntityManager<Milestone> {
	
	@Nullable
	Milestone find(Project project, String name);
	
	void delete(Milestone milestone);

	Milestone find(String milestoneFQN);
	
}