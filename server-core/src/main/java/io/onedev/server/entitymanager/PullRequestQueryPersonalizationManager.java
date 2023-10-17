package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestQueryPersonalization;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PullRequestQueryPersonalizationManager extends EntityManager<PullRequestQueryPersonalization> {
	
	PullRequestQueryPersonalization find(Project project, User user);

    void create(PullRequestQueryPersonalization personalization);

	void update(PullRequestQueryPersonalization personalization);
	
}
