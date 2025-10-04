package io.onedev.server.service;

import java.util.List;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;

public interface PullRequestUpdateService extends EntityService<PullRequestUpdate> {
	
	void checkUpdate(PullRequest request);
	
	List<PullRequestUpdate> queryAfter(Long projectId, Long afterUpdateId, int count);

    void create(PullRequestUpdate update);
	
}
