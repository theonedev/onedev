package io.onedev.server.entitymanager;

import java.util.List;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestTouch;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestTouchManager extends EntityManager<PullRequestTouch> {
	
	void touch(Project project, Long requestId, boolean newRequest);
	
	List<PullRequestTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
