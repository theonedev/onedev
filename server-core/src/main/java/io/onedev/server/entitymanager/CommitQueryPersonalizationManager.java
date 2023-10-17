package io.onedev.server.entitymanager;

import io.onedev.server.model.CommitQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface CommitQueryPersonalizationManager extends EntityManager<CommitQueryPersonalization> {
	
	CommitQueryPersonalization find(Project project, User user);

    void create(CommitQueryPersonalization commitQueryPersonalization);

	void update(CommitQueryPersonalization commitQueryPersonalization);
	
}
