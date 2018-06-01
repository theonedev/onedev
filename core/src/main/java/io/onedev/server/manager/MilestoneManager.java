package io.onedev.server.manager;

import javax.annotation.Nullable;

import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

public interface MilestoneManager extends EntityManager<Milestone> {
	
	Milestone find(Project project, String name);
	
	void delete(Milestone milestone, @Nullable Milestone moveIssuesToMilestone);
	
	void close(Milestone milestone, @Nullable Milestone moveOpenIssuesToMilestone);
	
	void updateIssueCount(Milestone milestone, boolean closed);
}
