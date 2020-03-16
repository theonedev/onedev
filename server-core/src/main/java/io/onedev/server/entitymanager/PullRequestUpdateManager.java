package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestUpdateManager extends EntityManager<PullRequestUpdate> {
	
	void checkUpdate(PullRequest request);
	
	List<PullRequestUpdate> queryAfter(Project project, @Nullable Long afterUpdateId, int count);
}
