package io.onedev.server.manager;

import io.onedev.server.model.IssueTouch;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.List;

public interface IssueTouchManager extends EntityManager<IssueTouch> {
	
	void touch(Project project, Long issueId);
	
	List<IssueTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
