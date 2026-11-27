package io.onedev.server.service;

import java.util.List;

import io.onedev.server.model.PullRequestUpdate;

public interface PullRequestUpdateService extends EntityService<PullRequestUpdate> {

	List<PullRequestUpdate> queryAfter(Long projectId, Long afterUpdateId, int count);

}
