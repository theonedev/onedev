package io.onedev.server.entitymanager;

import io.onedev.server.model.BuildQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface BuildQueryPersonalizationManager extends EntityManager<BuildQueryPersonalization> {
	
	BuildQueryPersonalization find(Project project, User user);

    void createOrUpdate(BuildQueryPersonalization buildQueryPersonalization);
	
}
