package io.onedev.server.service;

import io.onedev.server.model.BuildQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public interface BuildQueryPersonalizationService extends EntityService<BuildQueryPersonalization> {
	
	BuildQueryPersonalization find(Project project, User user);

    void createOrUpdate(BuildQueryPersonalization buildQueryPersonalization);
	
}
