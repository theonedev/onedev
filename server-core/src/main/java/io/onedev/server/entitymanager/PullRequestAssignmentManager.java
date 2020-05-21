package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestAssignmentManager extends EntityManager<PullRequestAssignment> {
	
	void addAssignee(PullRequestAssignment assignment);
	
	void removeAssignee(PullRequestAssignment assignment);
}
