package io.onedev.server.service;

import io.onedev.server.model.CommitQueryPersonalization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public interface CommitQueryPersonalizationService extends EntityService<CommitQueryPersonalization> {
	
	CommitQueryPersonalization find(Project project, User user);

    void createOrUpdate(CommitQueryPersonalization commitQueryPersonalization);
	
}
