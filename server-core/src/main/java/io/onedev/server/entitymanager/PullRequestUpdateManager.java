package io.onedev.server.entitymanager;

import java.util.List;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestUpdateManager extends EntityManager<PullRequestUpdate> {
	
	void checkUpdate(PullRequest request);
	
	List<PullRequestUpdate> queryAfter(Long projectId, Long afterUpdateId, int count);
	
}
