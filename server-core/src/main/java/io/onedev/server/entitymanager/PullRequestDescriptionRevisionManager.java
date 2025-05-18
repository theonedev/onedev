package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequestDescriptionRevision;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestDescriptionRevisionManager extends EntityManager<PullRequestDescriptionRevision> {
		
	void create(PullRequestDescriptionRevision revision);
	
}
