package io.onedev.server.service;

import io.onedev.server.model.PackQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public interface PackQueryPersonalizationService extends EntityService<PackQueryPersonalization> {
	
	PackQueryPersonalization find(Project project, User user);

    void createOrUpdate(PackQueryPersonalization personalization);
	
}
