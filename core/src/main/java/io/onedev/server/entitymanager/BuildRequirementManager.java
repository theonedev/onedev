package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.BuildRequirement;
import io.onedev.server.persistence.dao.EntityManager;

public interface BuildRequirementManager extends EntityManager<BuildRequirement> {

	void saveBuildRequirements(PullRequest request);
	
}
