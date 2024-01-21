package io.onedev.server.entitymanager;

import io.onedev.server.model.IssueTouch;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;
import java.util.List;

public interface IssueTouchManager extends EntityManager<IssueTouch> {
	
	void touch(Project project, Collection<Long> issueIds, boolean newIssues);
	
	List<IssueTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
