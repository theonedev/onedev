package io.onedev.server.entitymanager;

import io.onedev.server.model.PackQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PackQueryPersonalizationManager extends EntityManager<PackQueryPersonalization> {
	
	PackQueryPersonalization find(Project project, User user);

    void createOrUpdate(PackQueryPersonalization personalization);
	
}
