package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestTouch;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.List;

public interface PullRequestTouchManager extends EntityManager<PullRequestTouch> {
	
	void touch(Project project, Long requestId);

	List<PullRequestTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
