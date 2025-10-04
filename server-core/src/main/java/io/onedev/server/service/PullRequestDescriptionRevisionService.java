package io.onedev.server.service;

import io.onedev.server.model.PullRequestDescriptionRevision;

public interface PullRequestDescriptionRevisionService extends EntityService<PullRequestDescriptionRevision> {
		
	void create(PullRequestDescriptionRevision revision);
	
}
