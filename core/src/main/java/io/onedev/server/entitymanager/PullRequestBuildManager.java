package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestBuildManager extends EntityManager<PullRequestBuild> {

	void saveBuilds(PullRequest request);
	
}
